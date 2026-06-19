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
        String usbToken = request.getParameter("usbToken");

        if (phone == null || "".equals(phone) || pwd == null || "".equals(pwd)) {
            result.put("code", 0);
            result.put("msg", "账号密码不能为空");
            response.getWriter().write(result.toString());
            return;
        }

        String md5Pwd = MD5Util.md5(pwd);
        String md5UsbToken = (usbToken != null && !"".equals(usbToken)) ? MD5Util.md5(usbToken) : null;
        HttpSession session = request.getSession();

        try {
            System.out.println("[LoginServlet] 登录尝试: phone=" + phone + ", role=admin?");
            System.out.println("[LoginServlet] MD5密码: " + md5Pwd);
            System.out.println("[LoginServlet] MD5 USB: " + md5UsbToken);

            // 1. 尝试学员登录
            User user = userDAO.login(phone, md5Pwd);
            if (user != null) {
                System.out.println("[LoginServlet] 匹配到学员: " + user.getName());
                session.setAttribute("user", user);
                session.setAttribute("role", user.getRole());
                result.put("code", 1);
                result.put("msg", "登录成功");
                result.put("userId", user.getId());
                result.put("url", request.getContextPath() + "/pages/student.html");
                response.getWriter().write(result.toString());
                return;
            }

            // 2. 尝试工作人员登录（教练/管理员）
            Staff staff = staffDAO.login(phone, md5Pwd);
            System.out.println("[LoginServlet] staffDAO.login 结果: " + (staff != null ? staff.getName() : "null"));
            if (staff != null) {
                // 管理员需要验证 USB 令牌
                if ("admin".equals(staff.getRole())) {
                    String dbUsbToken = staff.getUsbToken();
                    if (dbUsbToken == null || dbUsbToken.isEmpty()) {
                        result.put("code", 0);
                        result.put("msg", "请输入USB安全令牌");
                        response.getWriter().write(result.toString());
                        return;
                    }
                    if (md5UsbToken == null || !dbUsbToken.equals(md5UsbToken)) {
                        result.put("code", 0);
                        result.put("msg", "USB安全令牌错误");
                        response.getWriter().write(result.toString());
                        return;
                    }
                }

                session.setAttribute("staff", staff);
                session.setAttribute("role", staff.getRole());
                result.put("code", 1);
                result.put("msg", "登录成功");
                result.put("userId", staff.getId());

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