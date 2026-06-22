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

            if (booking.getStudentScore() != null) {
                pstmt.setInt(8, booking.getStudentScore());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            if (booking.getCoachScore() != null) {
                pstmt.setInt(9, booking.getCoachScore());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }

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
                booking.setComment(rs.getString("comment"));
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
                booking.setComment(rs.getString("comment"));
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
                booking.setComment(rs.getString("comment"));
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
                booking.setComment(rs.getString("comment"));
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

    public int updateStudentScore(String bookingId, Integer studentScore) {
        String sql = "UPDATE booking SET studentScore = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentScore != null ? studentScore : 0);
            pstmt.setString(2, bookingId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    public List<Booking> findByCoachIdAndDate(String coachId, java.util.Date date) {
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());

        String sql = "SELECT * FROM booking WHERE coachId = ? AND status = 'approved' AND DATE(startTime) = ? ORDER BY startTime ASC";
        List<Booking> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, coachId);
            pstmt.setDate(2, sqlDate);
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
                booking.setComment(rs.getString("comment"));
                list.add(booking);
            }
            System.out.println("[BookingDAO] 查询到 " + list.size() + " 条已批准预约");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    // 按月统计预约数量（近6个月）
    public java.util.Map<String, Integer> countByMonth() {
        String sql = "SELECT DATE_FORMAT(startTime, '%Y-%m') AS month, COUNT(*) AS cnt FROM booking WHERE startTime >= DATE_SUB(NOW(), INTERVAL 6 MONTH) GROUP BY month ORDER BY month";
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

    // 评分分布统计（1-5分各有多少条）
    public java.util.Map<String, Integer> scoreDistribution(boolean isStudentScore) {
        String sql = isStudentScore
                ? "SELECT studentScore AS score, COUNT(*) AS cnt FROM booking WHERE studentScore IS NOT NULL AND studentScore > 0 GROUP BY studentScore ORDER BY studentScore"
                : "SELECT coachScore AS score, COUNT(*) AS cnt FROM booking WHERE coachScore IS NOT NULL AND coachScore > 0 GROUP BY coachScore ORDER BY coachScore";
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(String.valueOf(rs.getInt("score")), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return map;
    }

    //  统计每位教练的预约数量（排行）
    public java.util.Map<String, Integer> countByCoach() {
        String sql = "SELECT coachId, COUNT(*) AS cnt FROM booking GROUP BY coachId ORDER BY cnt DESC";
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("coachId"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return map;
    }

    // 按状态统计预约数量（分布）
    public java.util.Map<String, Integer> countByStatusGroup() {
        String sql = "SELECT status, COUNT(*) AS cnt FROM booking GROUP BY status";
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return map;
    }

    // 更新学员评分和评论
    public int updateStudentScoreAndComment(String bookingId, Integer studentScore, String comment) {
        String sql = "UPDATE booking SET studentScore = ?, comment = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentScore != null ? studentScore : 0);
            pstmt.setString(2, comment);
            pstmt.setString(3, bookingId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    public Booking findById(String id) {
        String sql = "SELECT * FROM booking WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
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
                booking.setComment(rs.getString("comment"));
                return booking;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询练车申请记录（带学员姓名、教练姓名，支持筛选）
     *
     * @param status 状态筛选（可选，为空则查询所有）
     * @param startDate 开始日期筛选（可选）
     * @param endDate 结束日期筛选（可选）
     * @param studentName 学员姓名模糊搜索（可选）
     * @return 练车申请列表
     */
    public List<Booking> findApplications(String status, java.util.Date startDate, java.util.Date endDate, String studentName) {
        StringBuilder sql = new StringBuilder("SELECT b.*, u.name AS studentName, s.name AS coachName FROM booking b ");
        sql.append("LEFT JOIN user u ON b.studentId = u.id ");
        sql.append("LEFT JOIN staff s ON b.coachId = s.id WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (status != null && !status.isEmpty()) {
            sql.append("AND b.status = ? ");
            params.add(status);
        }
        if (startDate != null) {
            sql.append("AND b.startTime >= ? ");
            params.add(new Timestamp(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append("AND b.startTime <= ? ");
            params.add(new Timestamp(endDate.getTime()));
        }
        if (studentName != null && !studentName.isEmpty()) {
            sql.append("AND u.name LIKE ? ");
            params.add("%" + studentName + "%");
        }

        sql.append("ORDER BY b.createTime DESC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Booking> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
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
                booking.setComment(rs.getString("comment"));
                // 附加字段
                booking.setStudentName(rs.getString("studentName"));
                booking.setCoachName(rs.getString("coachName"));
                list.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 统计待审核的练车申请数量
     */
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM booking WHERE status = 'pending'";
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
}
