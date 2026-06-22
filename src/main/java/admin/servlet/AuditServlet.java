package admin.servlet;

import admin.service.AdminService;
import common.entity.Staff;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.entity.User;
import common.util.UUIDUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 报名审核Servlet
 * API端点: /admin/audit
 */
@WebServlet("/admin/audit")
public class AuditServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

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

        try {
            if ("single".equals(action)) {
                handleSingleAudit(request, response, staff);
            } else if ("batch".equals(action)) {
                handleBatchAudit(request, response, staff);
            } else {
                result.put("code", 0);
                result.put("msg", "无效的操作，支持: single/batch");
                response.getWriter().write(result.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 0);
            result.put("msg", "系统异常: " + e.getMessage());
            response.getWriter().write(result.toString());
        }
    }

    /**
     * 处理单个审核
     */
    private void handleSingleAudit(HttpServletRequest request, HttpServletResponse response, Staff staff) throws IOException {
        JSONObject result = new JSONObject();
        String enrollmentId = request.getParameter("enrollmentId");
        String status = request.getParameter("status");
        String remark = request.getParameter("remark");

        AdminService.AuditResult auditResult = adminService.auditSingle(enrollmentId, status, remark, staff);

        if (auditResult.isSuccess()) {
            result.put("code", 1);
            result.put("msg", auditResult.getMessage());
        } else {
            result.put("code", 0);
            result.put("msg", auditResult.getMessage());
        }
        response.getWriter().write(result.toString());
    }

    /**
     * 处理批量审核
     */
    private void handleBatchAudit(HttpServletRequest request, HttpServletResponse response, Staff staff) throws IOException {
        JSONObject result = new JSONObject();
        String idsParam = request.getParameter("ids");
        String status = request.getParameter("status");
        String remark = request.getParameter("remark");

        if (idsParam == null || idsParam.isEmpty()) {
            result.put("code", 0);
            result.put("msg", "请选择要审核的报名记录");
            response.getWriter().write(result.toString());
            return;
        }

        List<String> idList = Arrays.asList(idsParam.split(","));
        AdminService.AuditResult auditResult = adminService.auditBatch(idList, status, remark, staff);

        if (auditResult.isSuccess()) {
            result.put("code", 1);
            result.put("msg", auditResult.getMessage());
            result.put("processedCount", auditResult.getProcessedCount());
        } else {
            result.put("code", 0);
            result.put("msg", auditResult.getMessage());
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

        Staff staff = (Staff) session.getAttribute("staff");
        if (!"admin".equals(staff.getRole())) {
            result.put("code", 0);
            result.put("msg", "权限不足");
            response.getWriter().write(result.toString());
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("statistics".equals(action)) {
                AdminService.AuditStatistics stat = adminService.getAuditStatistics();
                result.put("code", 1);
                result.put("data", stat);
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
