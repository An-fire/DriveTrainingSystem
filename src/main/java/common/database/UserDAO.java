package common.database;

import common.entity.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public User login(String phone, String pwd) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;
        String sql = "select * from user where phone=? and password=?";

        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            pstmt.setString(2, pwd);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                user = new User();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setIdCard(rs.getString("idCard"));
                user.setPhone(rs.getString("phone"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setSubject(rs.getString("subject"));
                user.setCreateTime(rs.getTimestamp("createTime"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return user;
    }

    public int insert(User user) {
        String sql = "INSERT INTO user(id, name, idCard, phone, password, role, subject, createTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getIdCard());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getPassword());
            pstmt.setString(6, user.getRole());
            pstmt.setString(7, user.getSubject());
            pstmt.setTimestamp(8, user.getCreateTime() != null ? new java.sql.Timestamp(user.getCreateTime().getTime()) : new java.sql.Timestamp(System.currentTimeMillis()));
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBCConnection.close(conn, pstmt);
        }
    }

    public User findByIdCard(String idCard) {
        String sql = "SELECT * FROM user WHERE idCard = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, idCard);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setIdCard(rs.getString("idCard"));
                user.setPhone(rs.getString("phone"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setSubject(rs.getString("subject"));
                user.setCreateTime(rs.getTimestamp("createTime"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    public User findByPhone(String phone) {
        String sql = "SELECT * FROM user WHERE phone = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setIdCard(rs.getString("idCard"));
                user.setPhone(rs.getString("phone"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setSubject(rs.getString("subject"));
                user.setCreateTime(rs.getTimestamp("createTime"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return null;
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM user ORDER BY createTime DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> list = new ArrayList<>();
        try {
            conn = DBCConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setIdCard(rs.getString("idCard"));
                user.setPhone(rs.getString("phone"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setSubject(rs.getString("subject"));
                user.setCreateTime(rs.getTimestamp("createTime"));
                list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCConnection.close(conn, pstmt, rs);
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM user";
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