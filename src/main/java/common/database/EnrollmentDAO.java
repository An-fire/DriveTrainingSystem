package common.database;

import common.entity.Enrollment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 报名数据访问层
 *
 * <p>提供报名记录的各种数据库操作，包括：
 * <ul>
 *   <li>报名申请创建</li>
 *   <li>待审核报名查询</li>
 *   <li>报名状态更新（审核）</li>
 *   <li>按学员/教练查询报名</li>
 * </ul>
 *
 * @author SuperDriver Team
 * @version 1.0
 */
public class EnrollmentDAO {

    /**
     * 插入新的报名记录
     *
     * @param enrollment 报名信息
     * @return 影响的行数，0表示插入失败
     */
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

    /**
     * 根据学员ID查询最新的报名记录
     *
     * @param studentId 学员ID
     * @return 报名记录，不存在则返回null
     */
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
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询所有待审核的报名记录
     *
     * @return 待审核报名列表，按申请时间升序排列
     */
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

    /**
     * 更新报名状态（无备注）
     *
     * @param id 报名ID
     * @param status 新状态
     * @return 影响的行数
     * @see #updateStatusWithAdmin(String, String, String, String) 带有管理员ID和备注的更新
     */
    public int updateStatus(String id, String status) {
        return updateStatusWithRemark(id, status, null);
    }

    /**
     * 更新报名状态（带备注）
     *
     * @param id 报名ID
     * @param status 新状态
     * @param remark 审核备注
     * @return 影响的行数
     * @see #updateStatusWithAdmin(String, String, String, String) 带有管理员ID和备注的更新
     */
    public int updateStatusWithRemark(String id, String status, String remark) {
        return updateStatusWithAdmin(id, status, remark, null);
    }

    /**
     * 更新报名状态（带备注和管理员ID）
     *
     * <p>此方法会在审核时记录审核的管理员信息，便于追溯审核责任。</p>
     *
     * @param id 报名ID
     * @param status 新状态（approved/rejected）
     * @param remark 审核备注/意见
     * @param adminId 审核管理员ID
     * @return 影响的行数，0表示更新失败
     */
    public int updateStatusWithAdmin(String id, String status, String remark, String adminId) {
        String sql = "UPDATE enrollment SET status = ?, auditTime = NOW(), auditRemark = ?, adminId = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setString(2, remark);
            pstmt.setString(3, adminId);
            pstmt.setString(4, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    /**
     * 批量更新报名状态
     *
     * @param ids 报名ID列表
     * @param status 新状态
     * @param remark 审核备注
     * @return 成功更新的记录数
     * @see #batchUpdateStatusWithAdmin(List, String, String, String) 带有管理员ID的批量更新
     */
    public int batchUpdateStatus(List<String> ids, String status, String remark) {
        return batchUpdateStatusWithAdmin(ids, status, remark, null);
    }

    /**
     * 批量更新报名状态（带管理员ID）
     *
     * @param ids 报名ID列表
     * @param status 新状态
     * @param remark 审核备注
     * @param adminId 审核管理员ID
     * @return 成功更新的记录数
     */
    public int batchUpdateStatusWithAdmin(List<String> ids, String status, String remark, String adminId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        String sql = "UPDATE enrollment SET status = ?, auditTime = NOW(), auditRemark = ?, adminId = ? WHERE id = ? AND status = 'pending'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int total = 0;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            for (String id : ids) {
                pstmt.setString(1, status);
                pstmt.setString(2, remark);
                pstmt.setString(3, adminId);
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

    /**
     * 根据教练ID查询报名记录
     *
     * @param coachId 教练ID
     * @return 报名列表
     */
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

    /**
     * 查询所有报名记录
     *
     * @return 报名列表，按申请时间降序排列
     */
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

    /**
     * 查询所有报名记录（包含学员和教练信息，使用JOIN优化查询）
     * 避免N+1查询问题，一次SQL获取所有数据
     *
     * @return 报名列表（包含关联的学员和教练信息），按申请时间降序排列
     */
    public List<java.util.Map<String, Object>> findAllWithDetails() {
        String sql = "SELECT e.id, e.studentId, e.coachId, e.subjectType, e.status, " +
                     "e.applyTime, e.auditTime, e.auditRemark, e.adminId, " +
                     "s.name AS studentName, s.phone AS studentPhone, s.idCard AS studentIdCard, " +
                     "c.name AS coachName, c.phone AS coachPhone " +
                     "FROM enrollment e " +
                     "LEFT JOIN user s ON e.studentId = s.id " +
                     "LEFT JOIN staff c ON e.coachId = c.id " +
                     "ORDER BY e.applyTime DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            while (rs.next()) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", rs.getString("id"));
                map.put("studentId", rs.getString("studentId"));
                map.put("coachId", rs.getString("coachId"));
                map.put("subjectType", rs.getString("subjectType"));
                map.put("status", rs.getString("status"));
                map.put("applyTimeStr", rs.getTimestamp("applyTime") != null ? sdf.format(rs.getTimestamp("applyTime")) : null);
                map.put("auditRemark", rs.getString("auditRemark"));
                map.put("adminId", rs.getString("adminId"));
                map.put("studentName", rs.getString("studentName"));
                map.put("studentPhone", rs.getString("studentPhone"));
                map.put("studentIdCard", rs.getString("studentIdCard"));
                map.put("coachName", rs.getString("coachName"));
                map.put("coachPhone", rs.getString("coachPhone"));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据状态统计报名数量
     *
     * @param status 报名状态
     * @return 符合状态的记录数
     */
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

    /**
     * 根据ID查询报名记录
     *
     * @param id 报名ID
     * @return 报名记录，不存在则返回null
     */
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

    /**
     * 将ResultSet映射为Enrollment对象
     *
     * @param rs 数据库结果集
     * @return Enrollment对象
     * @throws SQLException SQL异常
     */
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
        enrollment.setAdminId(rs.getString("adminId"));
        return enrollment;
    }

    /**
     * 按月统计报名数量（近6个月趋势）
     *
     * @return 月份->数量的Map，按月份升序排列
     */
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
