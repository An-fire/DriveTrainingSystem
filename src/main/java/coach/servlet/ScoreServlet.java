package coach.servlet;

import com.alibaba.fastjson.JSONObject;
import coach.service.CoachService;
import coach.util.CoachValidator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 教练提交评分
 * 请求方式：POST
 * 请求体：JSON
 * {
 *     "bookingId": "预约UUID",
 *     "coachId": "教练UUID",
 *     "score": 4,
 *     "canExam": true
 * }
 * 返回格式：JSON
 */
@WebServlet("/coach/score")
public class ScoreServlet extends HttpServlet {

    private final CoachService coachService = new CoachService();

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

        JSONObject params;
        try {
            params = JSONObject.parseObject(sb.toString());
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", "请求参数格式错误，请使用JSON格式");
            resp.getWriter().write(result.toString());
            return;
        }

        String bookingId = params.getString("bookingId");
        String coachId = params.getString("coachId");
        Integer score = params.getInteger("score");
        Boolean canExam = params.getBoolean("canExam");

        // 使用 CoachValidator 综合校验所有参数
        String error = CoachValidator.validateScoreRequest(coachId, bookingId, score, canExam);
        if (error != null) {
            result.put("code", 400);
            result.put("message", error);
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