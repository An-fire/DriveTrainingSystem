package common.database;

import common.entity.Enrollment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {

    public int insert(Enrollment enrollment) {
        String sql = "INSERT INTO enrollment(id, studentId, coachId, subjectType, status, applyTime, auditTime) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, enrollment.getId());
            pstmt.setString(2, enrollment.getStudentId());
            pstmt.setString(3, enrollment.getCoachId());
            pstmt.setString(4, enrollment.getSubjectType());
            pstmt.setString(5, enrollment.getStatus());
            pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            pstmt.setTimestamp(7, enrollment.getAuditTime() != null ? new Timestamp(enrollment.getAuditTime().getTime()) : null);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    public Enrollment findByStudentId(String studentId) {
        String sql = "SELECT * FROM enrollment WHERE studentId = ? ORDER BY applyTime DESC LIMIT 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Enrollment enrollment = new Enrollment();
                enrollment.setId(rs.getString("id"));
                enrollment.setStudentId(rs.getString("studentId"));
                enrollment.setCoachId(rs.getString("coachId"));
                enrollment.setSubjectType(rs.getString("subjectType"));
                enrollment.setStatus(rs.getString("status"));
                enrollment.setApplyTime(rs.getTimestamp("applyTime"));
                enrollment.setAuditTime(rs.getTimestamp("auditTime"));
                enrollment.setAuditRemark(rs.getString("auditRemark"));
                return enrollment;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    public List<Enrollment> findAllPending() {
        String sql = "SELECT * FROM enrollment WHERE status = 'pending' ORDER BY applyTime ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Enrollment> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public int updateStatus(String id, String status) {
        return updateStatusWithRemark(id, status, null);
    }

    public int updateStatusWithRemark(String id, String status, String remark) {
        String sql = "UPDATE enrollment SET status = ?, auditTime = ?, auditRemark = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(3, remark);
            pstmt.setString(4, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    public int batchUpdateStatus(List<String> ids, String status, String remark) {
        String sql = "UPDATE enrollment SET status = ?, auditTime = ?, auditRemark = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int total = 0;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            for (String id : ids) {
                pstmt.setString(1, status);
                pstmt.setTimestamp(2, now);
                pstmt.setString(3, remark);
                pstmt.setString(4, id);
                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch();
            for (int r : results) {
                total += (r > 0 ? r : 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt);
        }
        return total;
    }

    public List<Enrollment> findByCoachId(String coachId) {
        String sql = "SELECT * FROM enrollment WHERE coachId = ? ORDER BY applyTime DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Enrollment> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, coachId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public List<Enrollment> findAll() {
        String sql = "SELECT * FROM enrollment ORDER BY applyTime DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Enrollment> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM enrollment WHERE status = ?";
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

    public Enrollment findById(String id) {
        String sql = "SELECT * FROM enrollment WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    private Enrollment mapResultSet(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(rs.getString("id"));
        enrollment.setStudentId(rs.getString("studentId"));
        enrollment.setCoachId(rs.getString("coachId"));
        enrollment.setSubjectType(rs.getString("subjectType"));
        enrollment.setStatus(rs.getString("status"));
        enrollment.setApplyTime(rs.getTimestamp("applyTime"));
        enrollment.setAuditTime(rs.getTimestamp("auditTime"));
        enrollment.setAuditRemark(rs.getString("auditRemark"));
        return enrollment;
    }

    //  按统计报名数量（近6个月趋势）
    public java.util.Map<String, Integer> countByMonth() {
        String sql = "SELECT DATE_FORMAT(applyTime, '%Y-%m') AS month, COUNT(*) AS cnt FROM enrollment WHERE applyTime >= DATE_SUB(NOW(), INTERVAL 6 MONTH) GROUP BY month ORDER BY month";
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("month"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return map;
    }
}
