package auth;

import common.database.UserDAO;
import common.entity.User;
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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        String name = request.getParameter("name");
        String idCard = request.getParameter("idCard");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String subject = request.getParameter("subject");

        // 基本验证
        if (name == null || name.isEmpty() || idCard == null || idCard.isEmpty() ||
            phone == null || phone.isEmpty() || password == null || password.isEmpty()) {
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

        try {
            // 检查手机号是否已注册
            User phoneUser = userDAO.findByPhone(phone);
            if (phoneUser != null) {
                result.put("code", 0);
                result.put("msg", "该手机号已注册");
                response.getWriter().write(result.toString());
                return;
            }
            
            // 检查身份证是否已注册
            User existUser = userDAO.findByIdCard(idCard);
            if (existUser != null) {
                result.put("code", 0);
                result.put("msg", "该身份证号已注册");
                response.getWriter().write(result.toString());
                return;
            }

            // 创建用户
            User user = new User();
            user.setId(UUIDUtil.getUUID());
            user.setName(name);
            user.setIdCard(idCard);
            user.setPhone(phone);
            user.setPassword(MD5Util.md5(password));
            user.setRole("student");
            user.setSubject(subject != null ? subject : "C2");
            user.setCreateTime(new Date());

            int rows = userDAO.insert(user);
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
