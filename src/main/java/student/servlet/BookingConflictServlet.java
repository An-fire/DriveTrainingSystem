package student.servlet;

import common.util.Result;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import student.exception.StudentException;

import java.io.IOException;

@WebServlet("/student/booking/conflict")
public class BookingConflictServlet extends BaseStudentServlet {
    private final student.service.StudentService studentService = new student.service.StudentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 无需登录：预约提交前前端预校验冲突
        Result result;
        try {
            String coachId = request.getParameter("coachId");
            String start = request.getParameter("start");
            String end = request.getParameter("end");
            boolean conflict = studentService.checkCoachTimeConflict(coachId, start, end);
            result = Result.success(conflict);
        } catch (StudentException e) {
            result = Result.error(e.getMessage());
        }
        writeJson(response, result);
    }
}