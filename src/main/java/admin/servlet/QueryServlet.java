package admin.servlet;

import common.database.BookingDAO;
import common.database.EnrollmentDAO;
import common.database.StaffDAO;
import common.database.UserDAO;
import common.entity.Booking;
import common.entity.Enrollment;
import common.entity.Staff;
import common.entity.User;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/query")
public class QueryServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final StaffDAO staffDAO = new StaffDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("staff") == null) {
            result.put("code", 0);
            result.put("msg", "请先登录");
            response.getWriter().write(result.toString());
            return;
        }

        Staff staff = (Staff) session.getAttribute("staff");
        if (!"admin".equals(staff.getRole())) {
            result.put("code", 0);
            result.put("msg", "权限不足");
            response.getWriter().write(result.toString());
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("students".equals(action)) {
                List<User> list = userDAO.findAll();
                JSONArray array = JSONArray.parseArray(JSONArray.toJSONString(list));
                result.put("code", 1);
                result.put("data", array);
            } else if ("coaches".equals(action)) {
                List<Staff> list = staffDAO.findAllCoaches();
                JSONArray array = JSONArray.parseArray(JSONArray.toJSONString(list));
                result.put("code", 1);
                result.put("data", array);
            } else if ("enrollments".equals(action)) {
                List<Enrollment> list = enrollmentDAO.findAll();
                JSONArray array = JSONArray.parseArray(JSONArray.toJSONString(list));
                result.put("code", 1);
                result.put("data", array);
            } else if ("enrollmentDetail".equals(action)) {
                String studentId = request.getParameter("studentId");
                if (studentId != null && !studentId.isEmpty()) {
                    Enrollment enrollment = enrollmentDAO.findByStudentId(studentId);
                    if (enrollment != null) {
                        result.put("code", 1);
                        result.put("data", enrollment);
                    } else {
                        result.put("code", 1);
                        result.put("data", null);
                        result.put("msg", "暂无报名记录");
                    }
                } else {
                    result.put("code", 0);
                    result.put("msg", "参数错误");
                }
            } else if ("bookings".equals(action)) {
                String status = request.getParameter("status");
                List<Booking> list;
                if (status != null && !status.isEmpty()) {
                    list = bookingDAO.findByStatus(status);
                } else {
                    list = bookingDAO.findAll();
                }
                JSONArray array = JSONArray.parseArray(JSONArray.toJSONString(list));
                result.put("code", 1);
                result.put("data", array);
            } else if ("bookingDetail".equals(action)) {
                String studentId = request.getParameter("studentId");
                if (studentId != null && !studentId.isEmpty()) {
                    List<Booking> list = bookingDAO.findByStudentId(studentId);
                    JSONArray array = JSONArray.parseArray(JSONArray.toJSONString(list));
                    result.put("code", 1);
                    result.put("data", array);
                } else {
                    result.put("code", 0);
                    result.put("msg", "参数错误");
                }
            } else if ("stats".equals(action)) {
                JSONObject stats = new JSONObject();
                stats.put("totalStudents", userDAO.countAll());
                stats.put("totalCoaches", staffDAO.countCoaches());
                stats.put("pendingEnrollments", enrollmentDAO.countByStatus("pending"));
                stats.put("approvedEnrollments", enrollmentDAO.countByStatus("approved"));
                stats.put("totalBookings", bookingDAO.countAll());
                stats.put("approvedBookings", bookingDAO.countByStatus("approved"));
                result.put("code", 1);
                result.put("data", stats);
            } else if ("statsDetail".equals(action)) {
                JSONObject detail = new JSONObject();
                detail.put("studentSubject", userDAO.countBySubject());
                detail.put("coachSubject", staffDAO.countBySubject());
                detail.put("bookingMonth", bookingDAO.countByMonth());
                detail.put("studentScore", bookingDAO.scoreDistribution(true));
                detail.put("coachScore", bookingDAO.scoreDistribution(false));
                result.put("code", 1);
                result.put("data", detail);
            } else {
                result.put("code", 0);
                result.put("msg", "无效的操作");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 0);
            result.put("msg", "系统异常");
        }

        response.getWriter().write(result.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
