package student.servlet;

import com.alibaba.fastjson.JSONObject;
import common.database.BookingDAO;
import common.entity.Booking;
import common.entity.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/student/rate")
public class StudentRateServlet extends BaseStudentServlet {

    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        User loginUser = getLoginUser(req, resp);
        if (loginUser == null) {
            return;
        }

        try {
            Map<String, String> params = parseJsonParam(req.getReader());
            String bookingId = params.get("bookingId");
            String coachId = params.get("coachId");
            String scoreStr = params.get("studentScore");
            String comment = params.get("comment");

            // ===== 打印接收到的参数 =====
            System.out.println("=== StudentRateServlet 接收参数 ===");
            System.out.println("bookingId: " + bookingId);
            System.out.println("coachId: " + coachId);
            System.out.println("scoreStr: " + scoreStr);
            System.out.println("comment: " + comment);
            System.out.println("loginUser.id: " + loginUser.getId());

            int score = 0;
            if (scoreStr != null && !scoreStr.isEmpty()) {
                try {
                    score = Integer.parseInt(scoreStr);
                } catch (NumberFormatException e) {
                    score = 5;
                }
            }
            System.out.println("解析后的 score: " + score);

            // ===== 先查询该预约是否存在 =====
            Booking existingBooking = bookingDAO.findById(bookingId);
            System.out.println("数据库中查询到的预约: " + (existingBooking != null ? existingBooking.getId() : "null"));
            if (existingBooking != null) {
                System.out.println("现有 studentScore: " + existingBooking.getStudentScore());
                System.out.println("现有 comment: " + existingBooking.getComment());
            }

            // ===== 执行更新 =====
            int rows = bookingDAO.updateStudentScoreAndComment(bookingId, score, comment);
            System.out.println("updateStudentScoreAndComment 返回 rows: " + rows);

            if (rows > 0) {
                result.put("code", 1);
                result.put("msg", "评价成功，感谢您的反馈！");
            } else {
                // 如果 rows == 0，可能是 bookingId 不存在
                result.put("code", 0);
                result.put("msg", "评价失败，未找到对应的预约记录，请确认预约ID是否正确");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 0);
            result.put("msg", "系统异常: " + e.getMessage());
        }

        resp.getWriter().write(result.toString());
    }
}