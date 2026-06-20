package common.database;

import common.entity.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public int insert(Notification n) {
        String sql = "INSERT INTO notification(id, user_id, type, content, is_read, create_time) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, n.getId());
            pstmt.setString(2, n.getUserId());
            pstmt.setString(3, n.getType());
            pstmt.setString(4, n.getContent());
            pstmt.setInt(5, n.getIsRead() != null ? n.getIsRead() : 0);
            pstmt.setTimestamp(6, n.getCreateTime() != null ? new Timestamp(n.getCreateTime().getTime()) : new Timestamp(System.currentTimeMillis()));
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    public List<Notification> findByUserId(String userId, boolean unreadOnly) {
        String sql = "SELECT * FROM notification WHERE user_id = ?";
        if (unreadOnly) sql += " AND is_read = 0";
        sql += " ORDER BY create_time DESC";
        List<Notification> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getString("id"));
                n.setUserId(rs.getString("user_id"));
                n.setType(rs.getString("type"));
                n.setContent(rs.getString("content"));
                n.setIsRead(rs.getInt("is_read"));
                n.setCreateTime(rs.getTimestamp("create_time"));
                list.add(n);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public int markAsRead(String id) {
        String sql = "UPDATE notification SET is_read = 1 WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    public int countUnread(String userId) {
        String sql = "SELECT COUNT(*) FROM notification WHERE user_id = ? AND is_read = 0";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return 0;
    }
}