package common.database;

import common.entity.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public int insert(Booking booking) {
        String sql = "INSERT INTO booking(id, studentId, coachId, subjectType, startTime, endTime, status, studentScore, coachScore, canExam, createTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, booking.getId());
            pstmt.setString(2, booking.getStudentId());
            pstmt.setString(3, booking.getCoachId());
            pstmt.setString(4, booking.getSubjectType());
            pstmt.setTimestamp(5, new Timestamp(booking.getStartTime().getTime()));
            pstmt.setTimestamp(6, new Timestamp(booking.getEndTime().getTime()));
            pstmt.setString(7, booking.getStatus());
            pstmt.setInt(8, booking.getStudentScore() != null ? booking.getStudentScore() : 0);
            pstmt.setInt(9, booking.getCoachScore() != null ? booking.getCoachScore() : 0);
            pstmt.setBoolean(10, booking.getCanExam() != null ? booking.getCanExam() : false);
            pstmt.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    public List<Booking> findByStudentId(String studentId) {
        String sql = "SELECT * FROM booking WHERE studentId = ? ORDER BY createTime DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Booking> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getString("id"));
                booking.setStudentId(rs.getString("studentId"));
                booking.setCoachId(rs.getString("coachId"));
                booking.setSubjectType(rs.getString("subjectType"));
                booking.setStartTime(rs.getTimestamp("startTime"));
                booking.setEndTime(rs.getTimestamp("endTime"));
                booking.setStatus(rs.getString("status"));
                booking.setStudentScore(rs.getInt("studentScore"));
                booking.setCoachScore(rs.getInt("coachScore"));
                booking.setCanExam(rs.getBoolean("canExam"));
                booking.setCreateTime(rs.getTimestamp("createTime"));
                list.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public List<Booking> findByCoachId(String coachId) {
        String sql = "SELECT * FROM booking WHERE coachId = ? ORDER BY startTime ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Booking> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, coachId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getString("id"));
                booking.setStudentId(rs.getString("studentId"));
                booking.setCoachId(rs.getString("coachId"));
                booking.setSubjectType(rs.getString("subjectType"));
                booking.setStartTime(rs.getTimestamp("startTime"));
                booking.setEndTime(rs.getTimestamp("endTime"));
                booking.setStatus(rs.getString("status"));
                booking.setStudentScore(rs.getInt("studentScore"));
                booking.setCoachScore(rs.getInt("coachScore"));
                booking.setCanExam(rs.getBoolean("canExam"));
                booking.setCreateTime(rs.getTimestamp("createTime"));
                list.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public int updateStatus(String id, String status) {
        String sql = "UPDATE booking SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setString(2, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    public int updateScores(String id, Integer studentScore, Integer coachScore, Boolean canExam) {
        String sql = "UPDATE booking SET studentScore = ?, coachScore = ?, canExam = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentScore != null ? studentScore : 0);
            pstmt.setInt(2, coachScore != null ? coachScore : 0);
            pstmt.setBoolean(3, canExam != null ? canExam : false);
            pstmt.setString(4, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    public boolean checkTimeConflict(String coachId, Timestamp startTime, Timestamp endTime, String excludeId) {
        String sql = "SELECT COUNT(*) FROM booking WHERE coachId = ? AND status != 'rejected' AND ((startTime < ? AND endTime > ?) OR (startTime < ? AND endTime > ?) OR (startTime >= ? AND endTime <= ?))";
        if (excludeId != null && !excludeId.isEmpty()) {
            sql += " AND id != ?";
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, coachId);
            pstmt.setTimestamp(2, endTime);
            pstmt.setTimestamp(3, startTime);
            pstmt.setTimestamp(4, endTime);
            pstmt.setTimestamp(5, startTime);
            pstmt.setTimestamp(6, startTime);
            pstmt.setTimestamp(7, endTime);
            if (excludeId != null && !excludeId.isEmpty()) {
                pstmt.setString(8, excludeId);
            }
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return false;
    }

    public List<Booking> findAll() {
        String sql = "SELECT * FROM booking ORDER BY startTime DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Booking> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getString("id"));
                booking.setStudentId(rs.getString("studentId"));
                booking.setCoachId(rs.getString("coachId"));
                booking.setSubjectType(rs.getString("subjectType"));
                booking.setStartTime(rs.getTimestamp("startTime"));
                booking.setEndTime(rs.getTimestamp("endTime"));
                booking.setStatus(rs.getString("status"));
                booking.setStudentScore(rs.getInt("studentScore"));
                booking.setCoachScore(rs.getInt("coachScore"));
                booking.setCanExam(rs.getBoolean("canExam"));
                booking.setCreateTime(rs.getTimestamp("createTime"));
                list.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public List<Booking> findByStatus(String status) {
        String sql = "SELECT * FROM booking WHERE status = ? ORDER BY startTime DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Booking> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getString("id"));
                booking.setStudentId(rs.getString("studentId"));
                booking.setCoachId(rs.getString("coachId"));
                booking.setSubjectType(rs.getString("subjectType"));
                booking.setStartTime(rs.getTimestamp("startTime"));
                booking.setEndTime(rs.getTimestamp("endTime"));
                booking.setStatus(rs.getString("status"));
                booking.setStudentScore(rs.getInt("studentScore"));
                booking.setCoachScore(rs.getInt("coachScore"));
                booking.setCanExam(rs.getBoolean("canExam"));
                booking.setCreateTime(rs.getTimestamp("createTime"));
                list.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM booking";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return 0;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM booking WHERE status = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return 0;
    }
}
