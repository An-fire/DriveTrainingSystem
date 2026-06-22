package coach.service;

import com.alibaba.fastjson.JSONObject;
import common.database.DBCConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CoachService {

    /**
     * 获取教练的待评分列表
     */
    public List<JSONObject> getPendingList(String coachId) {
        List<JSONObject> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBCConnection.getConnection();
            String sql = "SELECT b.id as bookingId, u.name as studentName, b.subjectType, b.startTime, b.endTime, b.status " +
                    "FROM booking b JOIN user u ON b.studentId = u.id " +
                    "WHERE b.coachId = ? AND b.status = 'approved' AND b.coachScore IS NULL " +
                    "ORDER BY b.startTime DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, coachId);
            rs = pstmt.executeQuery();

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            while (rs.next()) {
                JSONObject item = new JSONObject();
                item.put("bookingId", rs.getString("bookingId"));
                item.put("studentName", rs.getString("studentName"));
                item.put("subject", rs.getString("subjectType"));
                Timestamp start = rs.getTimestamp("startTime");
                Timestamp end = rs.getTimestamp("endTime");
                String timeSlot = sdf.format(start) + " - " + sdf.format(end);
                item.put("timeSlot", timeSlot);
                item.put("status", rs.getString("status"));
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 获取学员对教练的评价
     */
    public List<JSONObject> getComments(String coachId) {
        List<JSONObject> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBCConnection.getConnection();
            String sql = "SELECT u.name as studentName, b.studentScore, b.comment, b.createTime " +
                    "FROM booking b " +
                    "JOIN user u ON b.studentId = u.id " +
                    "WHERE b.coachId = ? AND (b.studentScore IS NOT NULL OR b.comment IS NOT NULL) " +
                    "ORDER BY b.createTime DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, coachId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                JSONObject item = new JSONObject();
                item.put("studentName", rs.getString("studentName"));
                item.put("studentScore", rs.getInt("studentScore"));
                String comment = rs.getString("comment");
                item.put("comment", comment != null ? comment : "");
                item.put("createTime", rs.getString("createTime"));
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 提交教练评分
     */
    public boolean submitScore(String bookingId, int score, boolean canExam) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBCConnection.getConnection();
            String sql = "UPDATE booking SET coachScore = ?, canExam = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, score);
            pstmt.setBoolean(2, canExam);
            pstmt.setString(3, bookingId);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            DBCConnection.close(conn, pstmt, null);
        }
    }
}