package student.servlet;

import com.alibaba.fastjson.JSONObject;
import common.database.BookingDAO;
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

        // 验证登录
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

            if (bookingId == null || bookingId.isEmpty() || scoreStr == null || scoreStr.isEmpty()) {
                result.put("code", 0);
                result.put("msg", "预约ID和评分不能为空");
                resp.getWriter().write(result.toString());
                return;
            }

            int score;
            try {
                score = Integer.parseInt(scoreStr);
            } catch (NumberFormatException e) {
                result.put("code", 0);
                result.put("msg", "评分必须是数字");
                resp.getWriter().write(result.toString());
                return;
            }

            if (score < 1 || score > 5) {
                result.put("code", 0);
                result.put("msg", "评分必须在1-5之间");
                resp.getWriter().write(result.toString());
                return;
            }

            int rows = bookingDAO.updateStudentScore(bookingId, score);
            if (rows > 0) {
                result.put("code", 1);
                result.put("msg", "评分成功，感谢您的评价！");
            } else {
                result.put("code", 0);
                result.put("msg", "评分失败，请确认预约记录是否存在且已通过练车");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 0);
            result.put("msg", "系统异常：" + e.getMessage());
        }

        resp.getWriter().write(result.toString());
    }
}