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
            // ✅ 1. 先检查身份证是否已注册
            User existUser = userDAO.findByIdCard(idCard);
            if (existUser != null) {
                JSONObject data = new JSONObject();
                data.put("registered", true);
                data.put("name", existUser.getName());
                data.put("phone", existUser.getPhone());
                data.put("subject", existUser.getSubject());
                data.put("enrollStatus", existUser.getEnrollStatus());

                // 报名状态说明
                String statusMsg;
                String status = existUser.getEnrollStatus();
                if (status == null) {
                    statusMsg = "该身份证号已注册，但尚未报名，请前往登录后报名";
                } else if ("pending".equals(status)) {
                    statusMsg = "该身份证号已注册，报名待审核中，请耐心等待";
                } else if ("approved".equals(status)) {
                    statusMsg = "该身份证号已注册，报名已通过，请登录查看";
                } else if ("rejected".equals(status)) {
                    statusMsg = "该身份证号已注册，报名被驳回，请重新提交报名";
                } else {
                    statusMsg = "该身份证号已注册，请直接登录";
                }
                data.put("message", statusMsg);

                result.put("code", 0);
                result.put("msg", statusMsg);
                result.put("data", data);
                response.getWriter().write(result.toString());
                return;
            }

            // ✅ 2. 检查手机号是否已注册
            User phoneUser = userDAO.findByPhone(phone);
            if (phoneUser != null) {
                result.put("code", 0);
                result.put("msg", "该手机号已注册，请直接登录");
                response.getWriter().write(result.toString());
                return;
            }

            // ✅ 3. 创建用户
            User user = new User();
            user.setId(UUIDUtil.getUUID());
            user.setName(name);
            user.setIdCard(idCard);
            user.setPhone(phone);
            user.setPassword(MD5Util.md5(password));
            user.setRole("student");
            user.setSubject(subject != null ? subject : "C2");
            user.setCreateTime(new Date());
            user.setEnrollStatus(null); // 新注册用户尚未报名

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