package coach.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.database.BookingDAO;
import common.entity.Booking;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet("/student/availability")
public class CoachAvailabilityServlet extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        String coachId = req.getParameter("coachId");
        String dateStr = req.getParameter("date"); // 格式 yyyy-MM-dd

        if (coachId == null || coachId.isEmpty()) {
            result.put("code", 0);
            result.put("msg", "教练ID不能为空");
            resp.getWriter().write(result.toString());
            return;
        }

        try {
            // 如果没有传日期，默认今天
            Date targetDate;
            if (dateStr == null || dateStr.isEmpty()) {
                targetDate = new Date();
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                targetDate = dateFormat.parse(dateStr);
            }

            // 获取该教练当天所有已批准的预约（status = 'approved'）
            // 我们无法直接从现有方法获取当天，需要查询全部然后过滤，或者写新的DAO方法。
            // 简单实现：查询该教练的所有 approved 预约，然后按天过滤。
            // 注意：BookingDAO 没有按教练和日期查询的方法，我们临时用 findAll 然后过滤（数据量不大时可行）
            // 更优：在 BookingDAO 增加 findByCoachIdAndDate

            // 这里为了演示，我们写一个新的DAO方法（见下方）
            List<Booking> bookings = bookingDAO.findByCoachIdAndDate(coachId, (java.sql.Date) targetDate);

            // 构建占用时间段列表
            JSONArray occupied = new JSONArray();
            for (Booking b : bookings) {
                JSONObject slot = new JSONObject();
                slot.put("start", sdf.format(b.getStartTime()));
                slot.put("end", sdf.format(b.getEndTime()));
                occupied.add(slot);
            }

            result.put("code", 1);
            result.put("data", occupied);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 0);
            result.put("msg", "系统异常：" + e.getMessage());
        }

        resp.getWriter().write(result.toString());
    }
}