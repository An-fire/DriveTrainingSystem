package student.servlet;

import common.entity.Booking;
import common.entity.User;
import common.util.Result;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import student.exception.StudentException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/student/booking")
public class BookingServlet extends BaseStudentServlet {
    private final student.service.StudentService studentService = new student.service.StudentService();

    // 查询我的预约（GET不变）
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User loginUser = getLoginUser(request, response);
        if (loginUser == null) return;
        Result result;
        try {
            List<Booking> list = studentService.listMyBooking(loginUser.getId());
            result = Result.success(list);
        } catch (StudentException e) {
            result = Result.error(e.getMessage());
        }
        writeJson(response, result);
    }

    // 新增预约（POST原有逻辑不变）
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User loginUser = getLoginUser(request, response);
        if (loginUser == null) return;
        Result result;
        try {
            Map<String, String> paramMap = parseJsonParam(request.getReader());
            String coachId = paramMap.get("coachId");
            String subjectType = paramMap.get("subjectType");
            String startTime = paramMap.get("startTime");
            String endTime = paramMap.get("endTime");
            int rows = studentService.addBooking(loginUser.getId(), coachId, subjectType, startTime, endTime);
            result = Result.success("预约成功");
        } catch (StudentException e) {
            result = Result.error(e.getMessage());
        }
        writeJson(response, result);
    }

    // 取消预约 DELETE 请求，调用cancelBooking
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User loginUser = getLoginUser(request, response);
        if (loginUser == null) return;
        Result result;
        try {
            String bookingId = request.getParameter("bookingId");
            studentService.cancelBooking(bookingId, loginUser.getId());
            result = Result.success("取消预约成功");
        } catch (StudentException e) {
            result = Result.error(e.getMessage());
        }
        writeJson(response, result);
    }
}