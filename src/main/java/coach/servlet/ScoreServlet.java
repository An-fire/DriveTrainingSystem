package coach.servlet;

import com.alibaba.fastjson.JSONObject;
import coach.service.CoachService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/coach/score")
public class ScoreServlet extends HttpServlet {

    private CoachService coachService = new CoachService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        // 读取 JSON 请求体
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader reader = req.getReader();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        JSONObject params = JSONObject.parseObject(sb.toString());

        String bookingId = params.getString("bookingId");
        Integer score = params.getInteger("score");
        Boolean canExam = params.getBoolean("canExam");

        if (bookingId == null || score == null) {
            result.put("code", 400);
            result.put("message", "预约ID和评分不能为空");
            resp.getWriter().write(result.toString());
            return;
        }

        if (score < 1 || score > 5) {
            result.put("code", 400);
            result.put("message", "评分必须在1-5之间");
            resp.getWriter().write(result.toString());
            return;
        }

        try {
            boolean success = coachService.submitScore(bookingId, score, canExam != null && canExam);
            if (success) {
                result.put("code", 200);
                result.put("message", "评分成功");
            } else {
                result.put("code", 404);
                result.put("message", "未找到该预约记录");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "系统异常：" + e.getMessage());
        }

        resp.getWriter().write(result.toString());
    }
}