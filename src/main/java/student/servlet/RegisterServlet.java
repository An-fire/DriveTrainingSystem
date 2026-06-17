package student.servlet;

import common.entity.User;
import common.util.Result;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import student.exception.StudentException;
import student.service.StudentService;

import java.io.IOException;
import java.util.Map;

@WebServlet("/student/register")
public class RegisterServlet extends BaseStudentServlet {
    private final StudentService studentService = new StudentService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
                result = Result.error("注册失败，请稍后重试");
            }
        } catch (StudentException e) {
            result = Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result = Result.error("系统异常：" + e.getMessage());
        }
        writeJson(response, result);
    }
}