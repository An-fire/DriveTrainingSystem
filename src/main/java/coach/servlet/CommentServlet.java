package coach.servlet;

import com.alibaba.fastjson.JSONObject;
import coach.service.CoachService;
import coach.util.CoachValidator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * 获取学员对教练的评价列表
 * 请求方式：GET
 * 请求参数：coachId（教练UUID）
 * 返回格式：JSON
 */
@WebServlet("/coach/comments")
public class CommentServlet extends HttpServlet {

    private final CoachService coachService = new CoachService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        String coachId = req.getParameter("coachId");

        // 使用 CoachValidator 校验教练ID
        String error = CoachValidator.validateCoachId(coachId);
        if (error != null) {
            result.put("code", 400);
            result.put("message", error);
            resp.getWriter().write(result.toString());
            return;
        }

        try {
            List<JSONObject> list = coachService.getComments(coachId);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", list);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "系统异常：" + e.getMessage());
        }

        resp.getWriter().write(result.toString());
    }
}