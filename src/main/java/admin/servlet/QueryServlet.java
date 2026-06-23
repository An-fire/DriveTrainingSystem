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
            if ("coaches".equals(action)) {
                List<Staff> list = staffDAO.findAllCoaches();
                JSONArray array = JSONArray.parseArray(JSONArray.toJSONString(list));
                result.put("code", 1);
                result.put("data", array);
            } else if ("enrollments".equals(action)) {
                List<Enrollment> list = enrollmentDAO.findAll();
                JSONArray array = new JSONArray();
                for (Enrollment e : list) {
                    JSONObject obj = (JSONObject) JSONObject.toJSON(e);
                    User student = userDAO.findById(e.getStudentId());
                    Staff coach = staffDAO.findById(e.getCoachId());
                    obj.put("studentName", student != null ? student.getName() : "");
                    obj.put("coachName", coach != null ? coach.getName() : "");
                    // 添加学员详细信息
                    obj.put("studentPhone", student != null ? student.getPhone() : "");
                    obj.put("studentIdCard", student != null ? student.getIdCard() : "");
                    obj.put("coachPhone", coach != null ? coach.getPhone() : "");
                    // 格式化申请时间
                    if (e.getApplyTime() != null) {
                        obj.put("applyTimeStr", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(e.getApplyTime()));
                    }
                    array.add(obj);
                }
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
            } else if ("drivingApplications".equals(action)) {
                // 练车申请记录查询
                String status = request.getParameter("status");
                String startDate = request.getParameter("startDate");
                String endDate = request.getParameter("endDate");
                String studentName = request.getParameter("studentName");

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.Date start = null;
                java.util.Date end = null;
                try {
                    if (startDate != null && !startDate.isEmpty()) {
                        start = sdf.parse(startDate);
                    }
                    if (endDate != null && !endDate.isEmpty()) {
                        end = sdf.parse(endDate);
                        // 设置为当天的23:59:59
                        end = new java.util.Date(end.getTime() + 86400000 - 1);
                    }
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }

                List<Booking> list = bookingDAO.findApplications(status, start, end, studentName);
                JSONArray array = new JSONArray();
                for (Booking b : list) {
                    JSONObject obj = (JSONObject) JSONObject.toJSON(b);
                    // 格式化时间
                    if (b.getStartTime() != null) {
                        obj.put("startTimeStr", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(b.getStartTime()));
                    }
                    if (b.getEndTime() != null) {
                        obj.put("endTimeStr", new java.text.SimpleDateFormat("HH:mm").format(b.getEndTime()));
                    }
                    if (b.getCreateTime() != null) {
                        obj.put("createTimeStr", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(b.getCreateTime()));
                    }
                    array.add(obj);
                }
                result.put("code", 1);
                result.put("data", array);
                // 统计待审核数量
                result.put("pendingCount", bookingDAO.countPending());
            } else if ("bookings".equals(action)) {
                String status = request.getParameter("status");
                List<Booking> list;
                if (status != null && !status.isEmpty()) {
                    list = bookingDAO.findByStatus(status);
                } else {
                    list = bookingDAO.findAll();
                }
                JSONArray array = new JSONArray();
                for (Booking b : list) {
                    JSONObject obj = (JSONObject) JSONObject.toJSON(b);
                    User student = userDAO.findById(b.getStudentId());
                    Staff coach = staffDAO.findById(b.getCoachId());
                    obj.put("studentName", student != null ? student.getName() : "");
                    obj.put("coachName", coach != null ? coach.getName() : "");
                    array.add(obj);
                }
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
