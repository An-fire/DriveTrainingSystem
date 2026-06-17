package auth;

import common.database.StaffDAO;
import common.entity.Staff;
import common.util.MD5Util;
import common.util.UUIDUtil;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@WebServlet("/register-staff")
public class RegisterStaffServlet extends HttpServlet {
    private final StaffDAO staffDAO = new StaffDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String subject = request.getParameter("subject");

        if (name == null || name.isEmpty() ||
            phone == null || phone.isEmpty() || password == null || password.isEmpty() || role == null || role.isEmpty()) {
            result.put("code", 0);
            result.put("msg", "请填写完整信息");
            response.getWriter().write(result.toString());
            return;
        }

        if (password.length() < 6) {
            result.put("code", 0);
            result.put("msg", "密码长度不能少于6位");
            response.getWriter().write(result.toString());
            return;
        }

        if (!"coach".equals(role) && !"admin".equals(role)) {
            result.put("code", 0);
            result.put("msg", "角色选择无效");
            response.getWriter().write(result.toString());
            return;
        }

        try {
            Staff exist = staffDAO.findByPhone(phone);
            if (exist != null) {
                result.put("code", 0);
                result.put("msg", "该手机号已注册");
                response.getWriter().write(result.toString());
                return;
            }

            Staff staff = new Staff();
            staff.setId(UUIDUtil.getUUID());
            staff.setName(name);
            staff.setPhone(phone);
            staff.setPassword(MD5Util.md5(password));
            staff.setRole(role);
            if ("coach".equals(role)) {
                staff.setSubject(subject);
            }
            staff.setCreateTime(new Date());

            int rows = staffDAO.insert(staff);
            if (rows > 0) {
                result.put("code", 1);
                result.put("msg", "注册成功，请登录");
            } else {
                result.put("code", 0);
                result.put("msg", "注册失败，请稍后重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 0);
            result.put("msg", "系统异常，请稍后重试");
        }

        response.getWriter().write(result.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
