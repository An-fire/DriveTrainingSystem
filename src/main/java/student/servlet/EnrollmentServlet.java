package student.servlet;

import common.entity.Enrollment;
import common.entity.Staff;
import common.entity.User;
import common.util.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import student.exception.StudentException;
import student.service.StudentService;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/student/enrollment")
public class EnrollmentServlet extends HttpServlet {
    private final StudentService studentService = new StudentService();

    private Map<String, String> parseJsonParam(BufferedReader br) throws IOException {
        Map<String, String> map = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=utf-8");
        Result result;
        String type = request.getParameter("type");
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("user");
        try {
            if ("coach".equals(type)) {
                List<Staff> coachList = studentService.listAllCoach();
                result = Result.success(coachList);
            } else if ("myEnroll".equals(type)) {
                if (loginUser == null) {
                    result = Result.error("请先登录");
                } else {
                    Enrollment enrollment = studentService.getMyEnroll(loginUser.getId());
                    result = Result.success(enrollment);
                }
            } else {
                result = Result.error("请求参数错误");
            }
        } catch (StudentException e) {
            result = Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result = Result.error("查询异常");
        }
        String jsonResp = "{\"success\":" + result.isSuccess() + ",\"msg\":\"" + result.getMsg() + "\",\"data\":" + (result.getData() == null ? "null" : "\"" + result.getData() + "\"") + "}";
        response.getWriter().write(jsonResp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=utf-8");
        Result result;
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("user");
        if (loginUser == null) {
            result = Result.error("请先登录");
            String jsonResp = "{\"success\":" + result.isSuccess() + ",\"msg\":\"" + result.getMsg() + "\",\"data\":null}";
            response.getWriter().write(jsonResp);
            return;
        }
        try {
            Map<String, String> paramMap = parseJsonParam(request.getReader());
            String coachId = paramMap.get("coachId");
            String subjectType = paramMap.get("subjectType");
            int rows = studentService.applyEnroll(loginUser.getId(), coachId, subjectType);
            if (rows > 0) {
                result = Result.success("报名提交成功，等待审核");
            } else {
                result = Result.error("报名失败");
            }
        } catch (StudentException e) {
            result = Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result = Result.error("提交异常");
        }
        String jsonResp = "{\"success\":" + result.isSuccess() + ",\"msg\":\"" + result.getMsg() + "\",\"data\":" + (result.getData() == null ? "null" : "\"" + result.getData() + "\"") + "}";
        response.getWriter().write(jsonResp);
    }
}