package student.servlet;

import common.entity.User;
import common.util.Result;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BaseStudentServlet extends HttpServlet {
    // 修复原JSON分割BUG：兼容value包含逗号场景
    protected Map<String, String> parseJsonParam(BufferedReader br) throws IOException {
        Map<String, String> res = new HashMap<>();
        String content = br.readLine();
        if (content == null || content.isBlank()) return res;
        content = content.trim();
        if (content.startsWith("{")) content = content.substring(1);
        if (content.endsWith("}")) content = content.substring(0, content.length() - 1);
        String[] pairs = content.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            pair = pair.trim();
            if (!pair.contains(":")) continue;
            int idx = pair.indexOf(":");
            String key = pair.substring(0, idx).trim().replace("\"", "");
            String val = pair.substring(idx + 1).trim().replace("\"", "");
            res.put(key, val);
        }
        return res;
    }

    // 统一校验学员登录，未登录直接返回JSON
    protected User getLoginUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        // 增加空指针防护：session可能为null
        if (session == null) {
            response.setContentType("application/json;charset=utf-8");
            String json = "{\"success\":false,\"msg\":\"请先登录学员账号\",\"data\":null}";
            response.getWriter().write(json);
            return null;
        }

        User user = (User) session.getAttribute("user");
        if (user == null || !"student".equals(user.getRole())) {
            response.setContentType("application/json;charset=utf-8");
            String json = "{\"success\":false,\"msg\":\"请先登录学员账号\",\"data\":null}";
            response.getWriter().write(json);
            return null;
        }
        return user;
    }

    // 统一输出Result JSON（修复data变量名错误 + 增强特殊字符转义）
    protected void writeJson(HttpServletResponse response, Result result) throws IOException {
        response.setContentType("application/json;charset=utf-8");

        // 处理data字段：null/字符串/复杂对象的JSON兼容
        String dataStr;
        if (result.getData() == null) {
            dataStr = "null";
        } else {
            // 转义双引号、换行、回车、制表符等特殊字符，避免JSON格式错误
            String rawData = result.getData().toString()
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            dataStr = "\"" + rawData + "\"";
        }

        // 转义msg中的特殊字符
        String msg = result.getMsg() == null ? "" : result.getMsg()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        // 关键修复：将错误的data改为正确的dataStr
        String json = "{\"success\":" + result.isSuccess() + ",\"msg\":\"" + msg + "\",\"data\":" + dataStr + "}";
        response.getWriter().write(json);
    }
}