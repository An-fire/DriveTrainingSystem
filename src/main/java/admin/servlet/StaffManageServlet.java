package admin.servlet;

import common.database.StaffDAO;
import common.entity.Staff;
import common.util.MD5Util;
import common.util.UUIDUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@WebServlet("/admin/staff")
public class StaffManageServlet extends HttpServlet {
    private final StaffDAO staffDAO = new StaffDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("staff") == null) {
            result.put("code", 0);
            result.put("msg", "请先登录");
            response.getWriter().write(result.toString());
            return;
        }

        Staff currentStaff = (Staff) session.getAttribute("staff");
        if (!"admin".equals(currentStaff.getRole())) {
            result.put("code", 0);
            result.put("msg", "权限不足，只有管理员可以操作");
            response.getWriter().write(result.toString());
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {
                String name = request.getParameter("name");
                String idCard = request.getParameter("idCard");
                String phone = request.getParameter("phone");
                String password = request.getParameter("password");
                String role = request.getParameter("role");
                String subject = request.getParameter("subject");

                if (name == null || name.isEmpty() || phone == null || phone.isEmpty() ||
                    password == null || password.isEmpty() || role == null || role.isEmpty()) {
                    result.put("code", 0);
                    result.put("msg", "请填写完整信息");
                    response.getWriter().write(result.toString());
                    return;
                }

                if (!"coach".equals(role) && !"admin".equals(role)) {
                    result.put("code", 0);
                    result.put("msg", "角色选择无效");
                    response.getWriter().write(result.toString());
                    return;
                }

                Staff exist = staffDAO.findByPhone(phone);
                if (exist != null) {
                    result.put("code", 0);
                    result.put("msg", "该手机号已存在");
                    response.getWriter().write(result.toString());
                    return;
                }

                Staff staff = new Staff();
                staff.setId(UUIDUtil.getUUID());
                staff.setName(name);
                staff.setIdCard(idCard);
                staff.setPhone(phone);
                staff.setPassword(MD5Util.md5(password));
                staff.setRole(role);
                if ("coach".equals(role)) {
                    staff.setSubject(subject != null ? subject : "C2");
                }
                staff.setCreateTime(new Date());

                int rows = staffDAO.insert(staff);
                if (rows > 0) {
                    result.put("code", 1);
                    result.put("msg", "添加成功");
                } else {
                    result.put("code", 0);
                    result.put("msg", "添加失败");
                }
            } else if ("update".equals(action)) {
                String id = request.getParameter("id");
                String name = request.getParameter("name");
                String idCard = request.getParameter("idCard");
                String phone = request.getParameter("phone");
                String role = request.getParameter("role");
                String subject = request.getParameter("subject");

                if (id == null || id.isEmpty() || name == null || name.isEmpty() ||
                    phone == null || phone.isEmpty() || role == null || role.isEmpty()) {
                    result.put("code", 0);
                    result.put("msg", "请填写完整信息");
                    response.getWriter().write(result.toString());
                    return;
                }

                Staff exist = staffDAO.findById(id);
                if (exist == null) {
                    result.put("code", 0);
                    result.put("msg", "员工不存在");
                    response.getWriter().write(result.toString());
                    return;
                }

                Staff phoneExist = staffDAO.findByPhone(phone);
                if (phoneExist != null && !phoneExist.getId().equals(id)) {
                    result.put("code", 0);
                    result.put("msg", "该手机号已被使用");
                    response.getWriter().write(result.toString());
                    return;
                }

                Staff staff = new Staff();
                staff.setId(id);
                staff.setName(name);
                staff.setIdCard(idCard);
                staff.setPhone(phone);
                staff.setPassword(exist.getPassword());
                staff.setRole(role);
                if ("coach".equals(role)) {
                    staff.setSubject(subject != null ? subject : "C2");
                } else {
                    staff.setSubject(null);
                }

                int rows = staffDAO.update(staff);
                if (rows > 0) {
                    result.put("code", 1);
                    result.put("msg", "更新成功");
                } else {
                    result.put("code", 0);
                    result.put("msg", "更新失败");
                }
            } else if ("delete".equals(action)) {
                String id = request.getParameter("id");

                if (id == null || id.isEmpty()) {
                    result.put("code", 0);
                    result.put("msg", "请选择要删除的员工");
                    response.getWriter().write(result.toString());
                    return;
                }

                if (id.equals(currentStaff.getId())) {
                    result.put("code", 0);
                    result.put("msg", "不能删除自己");
                    response.getWriter().write(result.toString());
                    return;
                }

                Staff exist = staffDAO.findById(id);
                if (exist == null) {
                    result.put("code", 0);
                    result.put("msg", "员工不存在");
                    response.getWriter().write(result.toString());
                    return;
                }

                int rows = staffDAO.delete(id);
                if (rows > 0) {
                    result.put("code", 1);
                    result.put("msg", "删除成功");
                } else {
                    result.put("code", 0);
                    result.put("msg", "删除失败");
                }
            } else if ("resetPassword".equals(action)) {
                String id = request.getParameter("id");
                String newPassword = request.getParameter("newPassword");

                if (id == null || id.isEmpty()) {
                    result.put("code", 0);
                    result.put("msg", "请选择要重置密码的员工");
                    response.getWriter().write(result.toString());
                    return;
                }

                if (newPassword == null || newPassword.length() < 6) {
                    result.put("code", 0);
                    result.put("msg", "密码长度不能少于6位");
                    response.getWriter().write(result.toString());
                    return;
                }

                Staff exist = staffDAO.findById(id);
                if (exist == null) {
                    result.put("code", 0);
                    result.put("msg", "员工不存在");
                    response.getWriter().write(result.toString());
                    return;
                }

                exist.setPassword(MD5Util.md5(newPassword));
                int rows = staffDAO.update(exist);
                if (rows > 0) {
                    result.put("code", 1);
                    result.put("msg", "密码重置成功");
                } else {
                    result.put("code", 0);
                    result.put("msg", "密码重置失败");
                }
            } else {
                result.put("code", 0);
                result.put("msg", "无效的操作");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 0);
            result.put("msg", "系统异常");
        }

        response.getWriter().write(result.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("staff") == null) {
            result.put("code", 0);
            result.put("msg", "请先登录");
            response.getWriter().write(result.toString());
            return;
        }

        Staff currentStaff = (Staff) session.getAttribute("staff");
        if (!"admin".equals(currentStaff.getRole())) {
            result.put("code", 0);
            result.put("msg", "权限不足");
            response.getWriter().write(result.toString());
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("list".equals(action)) {
                List<Staff> list = staffDAO.findAll();
                JSONArray array = JSONArray.parseArray(JSONArray.toJSONString(list));
                result.put("code", 1);
                result.put("data", array);
            } else if ("coachList".equals(action)) {
                List<Staff> list = staffDAO.findAllCoaches();
                JSONArray array = JSONArray.parseArray(JSONArray.toJSONString(list));
                result.put("code", 1);
                result.put("data", array);
            } else if ("detail".equals(action)) {
                String id = request.getParameter("id");
                if (id != null && !id.isEmpty()) {
                    Staff staff = staffDAO.findById(id);
                    if (staff != null) {
                        result.put("code", 1);
                        result.put("data", staff);
                    } else {
                        result.put("code", 0);
                        result.put("msg", "员工不存在");
                    }
                } else {
                    result.put("code", 0);
                    result.put("msg", "参数错误");
                }
            } else {
                result.put("code", 0);
                result.put("msg", "无效的操作");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 0);
            result.put("msg", "系统异常");
        }

        response.getWriter().write(result.toString());
    }
}
