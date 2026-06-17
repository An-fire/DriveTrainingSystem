package coach.servlet;

import com.alibaba.fastjson.JSONObject;
import coach.service.CoachService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/coach/pending")
public class PendingServlet extends HttpServlet {

    private CoachService coachService = new CoachService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        String coachId = req.getParameter("coachId");

        if (coachId == null || coachId.isEmpty()) {
            result.put("code", 400);
            result.put("message", "教练ID不能为空");
            resp.getWriter().write(result.toString());
            return;
        }

        try {
            List<JSONObject> list = coachService.getPendingList(coachId);
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