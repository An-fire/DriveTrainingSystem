package coach.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.database.BookingDAO;
import common.database.StaffDAO;
import common.database.UserDAO;
import common.entity.Booking;
import common.entity.Staff;
import common.entity.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@WebServlet("/coach/bookings")
public class CoachBookingsServlet extends HttpServlet {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final StaffDAO staffDAO = new StaffDAO();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("staff") == null) {
            result.put("code", 0);
            result.put("msg", "请先登录");
            resp.getWriter().write(result.toString());
            return;
        }

        Staff staff = (Staff) session.getAttribute("staff");
        if (!"coach".equals(staff.getRole())) {
            result.put("code", 0);
            result.put("msg", "权限不足");
            resp.getWriter().write(result.toString());
            return;
        }

        String coachId = req.getParameter("coachId");
        String dateStr = req.getParameter("date");

        // 只能查看自己的预约
        if (coachId == null || !coachId.equals(staff.getId())) {
            coachId = staff.getId();
        }

        List<Booking> bookings;
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                java.util.Date date = sdf.parse(dateStr);
                bookings = bookingDAO.findByCoachIdAndDate(coachId, date);
            } catch (Exception e) {
                bookings = bookingDAO.findByCoachId(coachId);
            }
        } else {
            bookings = bookingDAO.findByCoachId(coachId);
        }

        JSONArray array = new JSONArray();
        for (Booking b : bookings) {
            JSONObject obj = (JSONObject) JSONObject.toJSON(b);
            User student = userDAO.findById(b.getStudentId());
            obj.put("studentName", student != null ? student.getName() : "");
            array.add(obj);
        }

        result.put("code", 1);
        result.put("data", array);
        resp.getWriter().write(result.toString());
    }
}