package student.servlet;

import com.alibaba.fastjson.JSON;
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

    protected User getLoginUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
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

    // ✅ 修复：使用 fastjson 正确序列化
    protected void writeJson(HttpServletResponse response, Result result) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        // 使用 fastjson 将整个 Result 对象转换为 JSON
        String json = JSON.toJSONString(result);
        response.getWriter().write(json);
    }
}