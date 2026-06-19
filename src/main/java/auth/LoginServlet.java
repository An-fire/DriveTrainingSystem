package auth;

import common.database.StaffDAO;
import common.database.UserDAO;
import common.entity.Staff;
import common.entity.User;
import common.util.MD5Util;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final StaffDAO staffDAO = new StaffDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        String phone = request.getParameter("phone");
        String pwd = request.getParameter("password");

        if (phone == null || "".equals(phone) || pwd == null || "".equals(pwd)) {
            result.put("code", 0);
            result.put("msg", "账号密码不能为空");
            response.getWriter().write(result.toString());
            return;
        }

        String md5Pwd = MD5Util.md5(pwd);
        HttpSession session = request.getSession();

        try {
            // 1. 尝试学员登录
            User user = userDAO.login(phone, md5Pwd);
            if (user != null) {
                session.setAttribute("user", user);
                session.setAttribute("role", user.getRole());
                result.put("code", 1);
                result.put("msg", "登录成功");
                result.put("userId", user.getId());  // ✅ 返回学员真实 ID
                result.put("url", request.getContextPath() + "/pages/student.html");
                response.getWriter().write(result.toString());
                return;
            }

            // 2. 尝试工作人员登录（教练/管理员）
            Staff staff = staffDAO.login(phone, md5Pwd);
            if (staff != null) {
                session.setAttribute("staff", staff);
                session.setAttribute("role", staff.getRole());
                result.put("code", 1);
                result.put("msg", "登录成功");
                result.put("userId", staff.getId());  // ✅ 返回工作人员真实 ID

                if ("admin".equals(staff.getRole())) {
                    result.put("url", request.getContextPath() + "/pages/admin.html");
                } else {
                    result.put("url", request.getContextPath() + "/pages/coach.html");
                }
                response.getWriter().write(result.toString());
                return;
            }

            // 3. 账号或密码错误
            result.put("code", 0);
            result.put("msg", "账号或密码错误");

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
