package admin.servlet;

import common.database.EnrollmentDAO;
import common.database.NotificationDAO;
import common.database.UserDAO;
import common.entity.Enrollment;
import common.entity.Notification;
import common.entity.Staff;
import common.entity.User;
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

@WebServlet("/admin/audit")
public class AuditServlet extends HttpServlet {
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

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

        Staff staff = (Staff) session.getAttribute("staff");
        if (!"admin".equals(staff.getRole())) {
            result.put("code", 0);
            result.put("msg", "权限不足");
            response.getWriter().write(result.toString());
            return;
        }

        String action = request.getParameter("action");
        String enrollmentId = request.getParameter("enrollmentId");
        String status = request.getParameter("status");

        try {
            if ("approve".equals(action)) {
                if (enrollmentId == null || enrollmentId.isEmpty()) {
                    result.put("code", 0);
                    result.put("msg", "请选择要审核的报名");
                    response.getWriter().write(result.toString());
                    return;
                }
                int rows = enrollmentDAO.updateStatus(enrollmentId, "approved");
                if (rows > 0) {
                    syncEnrollStatusAndNotify(enrollmentId, "approved");
                    result.put("code", 1);
                    result.put("msg", "已通过该报名申请");
                } else {
                    result.put("code", 0);
                    result.put("msg", "操作失败");
                }
            } else if ("reject".equals(action)) {
                if (enrollmentId == null || enrollmentId.isEmpty()) {
                    result.put("code", 0);
                    result.put("msg", "请选择要审核的报名");
                    response.getWriter().write(result.toString());
                    return;
                }
                int rows = enrollmentDAO.updateStatus(enrollmentId, "rejected");
                if (rows > 0) {
                    syncEnrollStatusAndNotify(enrollmentId, "rejected");
                    result.put("code", 1);
                    result.put("msg", "已拒绝该报名申请");
                } else {
                    result.put("code", 0);
                    result.put("msg", "操作失败");
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

    private void syncEnrollStatusAndNotify(String enrollmentId, String status) {
        try {
            Enrollment enrollment = enrollmentDAO.findById(enrollmentId);
            if (enrollment == null) return;

            UserDAO userDAO = new UserDAO();
            User user = userDAO.findById(enrollment.getStudentId());
            if (user != null) {
                user.setEnrollStatus(status);
                userDAO.update(user);
            }

            Notification notif = new Notification();
            notif.setId(UUIDUtil.getUUID());
            notif.setUserId(user.getId());
            notif.setType("enroll_audit");
            String content = "您的报名已" + ("approved".equals(status) ? "通过审核，可以开始预约练车了" : "被拒绝，请重新提交报名申请");
            notif.setContent(content);
            notif.setIsRead(0);
            notif.setCreateTime(new Date());
            new NotificationDAO().insert(notif);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        Staff staff = (Staff) session.getAttribute("staff");
        if (!"admin".equals(staff.getRole())) {
            result.put("code", 0);
            result.put("msg", "权限不足");
            response.getWriter().write(result.toString());
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("pending".equals(action)) {
                List<Enrollment> list = enrollmentDAO.findAllPending();
                JSONArray array = JSONArray.parseArray(JSONArray.toJSONString(list));
                result.put("code", 1);
                result.put("data", array);
            } else if ("all".equals(action)) {
                List<Enrollment> list = enrollmentDAO.findAll();
                JSONArray array = JSONArray.parseArray(JSONArray.toJSONString(list));
                result.put("code", 1);
                result.put("data", array);
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