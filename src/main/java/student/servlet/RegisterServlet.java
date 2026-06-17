package student.servlet;

import common.util.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import student.exception.StudentException;
import student.service.StudentService;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/student/register")
public class RegisterServlet extends HttpServlet {
    private final StudentService studentService = new StudentService();

    // 手动解析前端传来的JSON字符串，不依赖Jackson
    private Map<String, String> parseJsonParam(BufferedReader br) throws IOException {
        Map<String, String> map = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();
        // 简易JSON键值提取（仅适配前端提交的单层字符串参数）
        json = json.replace("{", "").replace("}", "").replace("\"", "");
        String[] arr = json.split(",");
        for (String item : arr) {
            String[] kv = item.split(":");
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=utf-8");
        Result result;
        try {
            Map<String, String> paramMap = parseJsonParam(request.getReader());
            String name = paramMap.get("name");
            String idCard = paramMap.get("idCard");
            String phone = paramMap.get("phone");
            String password = paramMap.get("password");
            String subject = paramMap.get("subject");

            int rows = studentService.register(name, idCard, phone, password, subject);
            if (rows > 0) {
                result = Result.success("学员注册成功");
            } else {
                result = Result.error("注册失败");
            }
        } catch (StudentException e) {
            result = Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result = Result.error("系统异常：" + e.getMessage());
        }
        // 手动拼接Result标准JSON返回，不使用Jackson
        String jsonResp = "{\"success\":" + result.isSuccess() + ",\"msg\":\"" + result.getMsg() + "\",\"data\":" + (result.getData() == null ? "null" : "\"" + result.getData() + "\"") + "}";
        response.getWriter().write(jsonResp);
    }
}