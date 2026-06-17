package coach.servlet;

import com.alibaba.fastjson.JSONObject;
import common.database.DBCConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/coach/comments")
public class CommentServlet extends HttpServlet {

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

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBCConnection.getConnection();
            String sql = "SELECT u.name as studentName, b.studentScore, b.createTime " +
                    "FROM booking b " +
                    "JOIN user u ON b.studentId = u.id " +
                    "WHERE b.coachId = ? AND b.studentScore IS NOT NULL " +
                    "ORDER BY b.createTime DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, coachId);
            rs = pstmt.executeQuery();

            List<JSONObject> list = new ArrayList<>();
            while (rs.next()) {
                JSONObject item = new JSONObject();
                item.put("studentName", rs.getString("studentName"));
                item.put("studentScore", rs.getInt("studentScore"));
                item.put("createTime", rs.getString("createTime"));
                list.add(item);
            }

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", list);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("message", "系统异常：" + e.getMessage());
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }

        resp.getWriter().write(result.toString());
    }
}