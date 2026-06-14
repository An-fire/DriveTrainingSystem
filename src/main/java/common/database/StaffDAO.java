package common.database;

import common.entity.Staff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffDAO {
    // 工作人员(管理员/教练)登录
    public Staff login(String phone, String pwd) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Staff staff = null;
        String sql = "select * from t_staff where phone=? and password=?";

        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            pstmt.setString(2, pwd);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                staff = new Staff();
                staff.setId(rs.getString("id"));
                staff.setName(rs.getString("name"));
                staff.setPhone(rs.getString("phone"));
                staff.setRole(rs.getString("role"));
                staff.setSubject(rs.getString("subject"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return staff;
    }
}