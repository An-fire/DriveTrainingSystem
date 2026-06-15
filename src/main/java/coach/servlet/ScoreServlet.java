package coach.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/coach/pending")
public class ScoreServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        // TODO: 调用service查询待评分记录，暂时返回模拟数据
        out.write("{\"code\":200,\"data\":[{\"bookingId\":\"1\",\"studentName\":\"张三\",\"timeSlot\":\"2025-06-20 09:00-10:00\"}]}");
    }
}