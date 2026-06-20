package common.database;

import common.entity.CoachSchedule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoachScheduleDAO {

    // 根据教练ID查询所有排班
    public List<CoachSchedule> findByCoachId(String coachId) {
        String sql = "SELECT * FROM coach_schedule WHERE coach_id = ? ORDER BY weekday, start_time";
        List<CoachSchedule> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, coachId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CoachSchedule s = new CoachSchedule();
                s.setId(rs.getString("id"));
                s.setCoachId(rs.getString("coach_id"));
                s.setWeekday(rs.getInt("weekday"));
                s.setStartTime(rs.getTime("start_time"));
                s.setEndTime(rs.getTime("end_time"));
                s.setCreateTime(rs.getTimestamp("create_time"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    // 新增排班
    public int insert(CoachSchedule schedule) {
        String sql = "INSERT INTO coach_schedule(id, coach_id, weekday, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schedule.getId());
            pstmt.setString(2, schedule.getCoachId());
            pstmt.setInt(3, schedule.getWeekday());
            pstmt.setTime(4, schedule.getStartTime());
            pstmt.setTime(5, schedule.getEndTime());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    // 删除排班
    public int delete(String id) {
        String sql = "DELETE FROM coach_schedule WHERE id = ?";
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

    /**
     * 检查某教练在指定时间段是否有排班
     * @param coachId 教练ID
     * @param startTime 预约开始时间
     * @param endTime 预约结束时间
     * @return true=有排班，false=无排班
     */
    public boolean hasSchedule(String coachId, Timestamp startTime, Timestamp endTime) {
        // 获取星期几（1=周一, 7=周日）
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(startTime.getTime());
        int weekday = cal.get(java.util.Calendar.DAY_OF_WEEK); // 1=周日, 2=周一...
        int wd = (weekday == 1) ? 7 : weekday - 1; // 转为1=周一, 7=周日

        String sql = "SELECT COUNT(*) FROM coach_schedule WHERE coach_id = ? AND weekday = ? " +
                "AND start_time <= ? AND end_time >= ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, coachId);
            pstmt.setInt(2, wd);
            pstmt.setTime(3, new Time(startTime.getTime()));
            pstmt.setTime(4, new Time(endTime.getTime()));
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

    // 查询某教练在指定星期几的所有排班
    public List<CoachSchedule> findByCoachIdAndWeekday(String coachId, int weekday) {
        String sql = "SELECT * FROM coach_schedule WHERE coach_id = ? AND weekday = ? ORDER BY start_time";
        List<CoachSchedule> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, coachId);
            pstmt.setInt(2, weekday);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CoachSchedule s = new CoachSchedule();
                s.setId(rs.getString("id"));
                s.setCoachId(rs.getString("coach_id"));
                s.setWeekday(rs.getInt("weekday"));
                s.setStartTime(rs.getTime("start_time"));
                s.setEndTime(rs.getTime("end_time"));
                s.setCreateTime(rs.getTimestamp("create_time"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }
}