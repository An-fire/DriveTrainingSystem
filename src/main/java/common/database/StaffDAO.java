package common.database;

import common.entity.Staff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {
    public Staff login(String account, String pwd) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Staff staff = null;
        String sql = "select * from staff where (phone=? or name=?) and password=?";

        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, account);
            pstmt.setString(2, account);
            pstmt.setString(3, pwd);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                staff = new Staff();
                staff.setId(rs.getString("id"));
                staff.setName(rs.getString("name"));
                staff.setPhone(rs.getString("phone"));
                staff.setPassword(rs.getString("password"));
                staff.setRole(rs.getString("role"));
                staff.setSubject(rs.getString("subject"));
                staff.setUsbToken(rs.getString("usbToken"));
                staff.setCreateTime(rs.getTimestamp("createTime"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return staff;
    }

    public Staff findByPhone(String phone) {
        String sql = "SELECT * FROM staff WHERE phone = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Staff staff = new Staff();
                staff.setId(rs.getString("id"));
                staff.setName(rs.getString("name"));
                staff.setPhone(rs.getString("phone"));
                staff.setPassword(rs.getString("password"));
                staff.setRole(rs.getString("role"));
                staff.setSubject(rs.getString("subject"));
                staff.setUsbToken(rs.getString("usbToken"));
                staff.setCreateTime(rs.getTimestamp("createTime"));
                return staff;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    public List<Staff> findAllCoaches() {
        String sql = "SELECT * FROM staff WHERE role = 'coach' ORDER BY createTime DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Staff> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Staff staff = new Staff();
                staff.setId(rs.getString("id"));
                staff.setName(rs.getString("name"));
                staff.setPhone(rs.getString("phone"));
                staff.setPassword(rs.getString("password"));
                staff.setRole(rs.getString("role"));
                staff.setSubject(rs.getString("subject"));
                staff.setUsbToken(rs.getString("usbToken"));
                staff.setCreateTime(rs.getTimestamp("createTime"));
                list.add(staff);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public int insert(Staff staff) {
        String sql = "INSERT INTO staff(id, name, phone, password, role, subject, usbToken, passwordPlain, usbTokenPlain, createTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, staff.getId());
            pstmt.setString(2, staff.getName());
            pstmt.setString(3, staff.getPhone());
            pstmt.setString(4, staff.getPassword());
            pstmt.setString(5, staff.getRole());
            pstmt.setString(6, staff.getSubject());
            pstmt.setString(7, staff.getUsbToken());
            pstmt.setString(8, staff.getPasswordPlain());
            pstmt.setString(9, staff.getUsbTokenPlain());
            pstmt.setTimestamp(10, staff.getCreateTime() != null ? new java.sql.Timestamp(staff.getCreateTime().getTime()) : new java.sql.Timestamp(System.currentTimeMillis()));
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    public int delete(String id) {
        String sql = "DELETE FROM staff WHERE id = ?";
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

    public int update(Staff staff) {
        String sql = "UPDATE staff SET name=?, phone=?, password=?, role=?, subject=?, usbToken=? WHERE id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, staff.getName());
            pstmt.setString(2, staff.getPhone());
            pstmt.setString(3, staff.getPassword());
            pstmt.setString(4, staff.getRole());
            pstmt.setString(5, staff.getSubject());
            pstmt.setString(6, staff.getUsbToken());
            pstmt.setString(7, staff.getId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    public int countCoaches() {
        String sql = "SELECT COUNT(*) FROM staff WHERE role = 'coach'";
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

    public Staff findById(String id) {
        String sql = "SELECT * FROM staff WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Staff staff = new Staff();
                staff.setId(rs.getString("id"));
                staff.setName(rs.getString("name"));
                staff.setPhone(rs.getString("phone"));
                staff.setPassword(rs.getString("password"));
                staff.setRole(rs.getString("role"));
                staff.setSubject(rs.getString("subject"));
                staff.setUsbToken(rs.getString("usbToken"));
                staff.setCreateTime(rs.getTimestamp("createTime"));
                return staff;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    public List<Staff> findAll() {
        String sql = "SELECT * FROM staff ORDER BY createTime DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Staff> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Staff staff = new Staff();
                staff.setId(rs.getString("id"));
                staff.setName(rs.getString("name"));
                staff.setPhone(rs.getString("phone"));
                staff.setPassword(rs.getString("password"));
                staff.setRole(rs.getString("role"));
                staff.setSubject(rs.getString("subject"));
                staff.setUsbToken(rs.getString("usbToken"));
                staff.setCreateTime(rs.getTimestamp("createTime"));
                list.add(staff);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public List<Staff> search(String keyword, String role) {
        StringBuilder sql = new StringBuilder("SELECT * FROM staff WHERE 1=1");
        java.util.List<String> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR phone LIKE ?)");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        if (role != null && !role.trim().isEmpty() && !"all".equals(role)) {
            sql.append(" AND role = ?");
            params.add(role);
        }

        sql.append(" ORDER BY createTime DESC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Staff> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Staff staff = new Staff();
                staff.setId(rs.getString("id"));
                staff.setName(rs.getString("name"));
                staff.setPhone(rs.getString("phone"));
                staff.setPassword(rs.getString("password"));
                staff.setRole(rs.getString("role"));
                staff.setSubject(rs.getString("subject"));
                staff.setUsbToken(rs.getString("usbToken"));
                staff.setCreateTime(rs.getTimestamp("createTime"));
                list.add(staff);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public java.util.Map<String, Integer> countBySubject() {
        String sql = "SELECT subject, COUNT(*) AS cnt FROM staff WHERE role = 'coach' AND subject IS NOT NULL AND subject != '' GROUP BY subject";
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("subject"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return map;
    }
}