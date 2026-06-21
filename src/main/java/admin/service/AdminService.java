package admin.service;

import common.database.EnrollmentDAO;
import common.database.NotificationDAO;
import common.database.UserDAO;
import common.entity.Enrollment;
import common.entity.Notification;
import common.entity.Staff;
import common.entity.User;
import common.util.UUIDUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 管理员服务类 - 负责学员报名审核等管理功能
 */
public class AdminService {

    private final EnrollmentDAO enrollmentDAO;
    private final UserDAO userDAO;
    private final NotificationDAO notificationDAO;

    // 审核状态常量
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";

    public AdminService() {
        this.enrollmentDAO = new EnrollmentDAO();
        this.userDAO = new UserDAO();
        this.notificationDAO = new NotificationDAO();
    }

    // 用于测试的构造函数
    public AdminService(EnrollmentDAO enrollmentDAO, UserDAO userDAO, NotificationDAO notificationDAO) {
        this.enrollmentDAO = enrollmentDAO;
        this.userDAO = userDAO;
        this.notificationDAO = notificationDAO;
    }

    /**
     * 验证报名信息完整性
     */
    public ValidationResult validateEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            return ValidationResult.fail("报名信息不能为空");
        }
        if (isBlank(enrollment.getStudentId())) {
            return ValidationResult.fail("学员ID不能为空");
        }
        if (isBlank(enrollment.getCoachId())) {
            return ValidationResult.fail("教练ID不能为空");
        }
        if (isBlank(enrollment.getSubjectType())) {
            return ValidationResult.fail("科目类型不能为空");
        }
        if (!enrollment.getSubjectType().matches("^C[123]$")) {
            return ValidationResult.fail("科目类型无效，仅支持C1/C2/C3");
        }
        if (!STATUS_PENDING.equals(enrollment.getStatus())
                && !STATUS_APPROVED.equals(enrollment.getStatus())
                && !STATUS_REJECTED.equals(enrollment.getStatus())) {
            return ValidationResult.fail("报名状态无效");
        }
        return ValidationResult.success();
    }

    /**
     * 验证报名信息是否存在且处于待审核状态
     */
    public ValidationResult validateEnrollmentAuditable(String enrollmentId) {
        if (isBlank(enrollmentId)) {
            return ValidationResult.fail("报名ID不能为空");
        }
        Enrollment enrollment = enrollmentDAO.findById(enrollmentId);
        if (enrollment == null) {
            return ValidationResult.fail("报名信息不存在");
        }
        if (!STATUS_PENDING.equals(enrollment.getStatus())) {
            return ValidationResult.fail("该报名已处理，当前状态为: " + enrollment.getStatus());
        }
        return ValidationResult.success();
    }

    /**
     * 单个审核报名
     */
    public AuditResult auditSingle(String enrollmentId, String status, String remark, Staff auditor) {
        ValidationResult v = validateEnrollmentAuditable(enrollmentId);
        if (!v.isSuccess()) {
            return AuditResult.fail(v.getMessage());
        }
        if (!STATUS_APPROVED.equals(status) && !STATUS_REJECTED.equals(status)) {
            return AuditResult.fail("审核状态无效，仅支持 approved/rejected");
        }

        int rows = enrollmentDAO.updateStatusWithRemark(enrollmentId, status, remark);
        if (rows > 0) {
            notifyStudent(enrollmentId, status, remark);
            return AuditResult.success("审核完成", 1);
        }
        return AuditResult.fail("审核操作失败");
    }

    /**
     * 批量审核报名
     */
    public AuditResult auditBatch(List<String> enrollmentIds, String status, String remark, Staff auditor) {
        if (enrollmentIds == null || enrollmentIds.isEmpty()) {
            return AuditResult.fail("请选择要审核的报名记录");
        }
        if (!STATUS_APPROVED.equals(status) && !STATUS_REJECTED.equals(status)) {
            return AuditResult.fail("审核状态无效，仅支持 approved/rejected");
        }

        int validCount = 0;
        List<String> validIds = new ArrayList<>();
        for (String id : enrollmentIds) {
            ValidationResult v = validateEnrollmentAuditable(id);
            if (v.isSuccess()) {
                validIds.add(id);
                validCount++;
            }
        }

        if (validIds.isEmpty()) {
            return AuditResult.fail("所选报名记录均不可审核");
        }

        int rows = enrollmentDAO.batchUpdateStatus(validIds, status, remark);
        for (String id : validIds) {
            notifyStudent(id, status, remark);
        }

        String msg = validCount < enrollmentIds.size()
                ? String.format("审核完成，成功%d条，跳过%d条", validCount, enrollmentIds.size() - validCount)
                : "批量审核完成";
        return AuditResult.success(msg, validCount);
    }

    /**
     * 获取待审核报名列表
     */
    public List<Enrollment> getPendingEnrollments() {
        return enrollmentDAO.findAllPending();
    }

    /**
     * 获取全部报名列表
     */
    public List<Enrollment> getAllEnrollments() {
        return enrollmentDAO.findAll();
    }

    /**
     * 获取审核统计
     */
    public AuditStatistics getAuditStatistics() {
        AuditStatistics stat = new AuditStatistics();
        stat.setPendingCount(enrollmentDAO.countByStatus(STATUS_PENDING));
        stat.setApprovedCount(enrollmentDAO.countByStatus(STATUS_APPROVED));
        stat.setRejectedCount(enrollmentDAO.countByStatus(STATUS_REJECTED));
        return stat;
    }

    // ==================== 私有方法 ====================

    private void notifyStudent(String enrollmentId, String status, String remark) {
        try {
            Enrollment enrollment = enrollmentDAO.findById(enrollmentId);
            if (enrollment == null) return;

            User user = userDAO.findById(enrollment.getStudentId());
            if (user == null) return;

            String content = STATUS_APPROVED.equals(status)
                    ? "您的报名申请已通过审核" + formatRemark(remark)
                    : "您的报名申请未通过审核" + formatRemark(remark);

            Notification notification = new Notification();
            notification.setId(UUIDUtil.getUUID());
            notification.setUserId(user.getId());
            notification.setType("enrollment_audit");
            notification.setContent(content);
            notification.setIsRead(0);
            notification.setCreateTime(new Date());

            notificationDAO.insert(notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatRemark(String remark) {
        return isBlank(remark) ? "" : "，审核意见：" + remark;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    // ==================== 内部类 ====================

    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean success;
        private final String message;

        private ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    /**
     * 审核结果
     */
    public static class AuditResult {
        private final boolean success;
        private final String message;
        private final int processedCount;

        private AuditResult(boolean success, String message, int processedCount) {
            this.success = success;
            this.message = message;
            this.processedCount = processedCount;
        }

        public static AuditResult success(String message, int count) {
            return new AuditResult(true, message, count);
        }

        public static AuditResult fail(String message) {
            return new AuditResult(false, message, 0);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getProcessedCount() { return processedCount; }
    }

    /**
     * 审核统计
     */
    public static class AuditStatistics {
        private int pendingCount;
        private int approvedCount;
        private int rejectedCount;

        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
        public int getApprovedCount() { return approvedCount; }
        public void setApprovedCount(int approvedCount) { this.approvedCount = approvedCount; }
        public int getRejectedCount() { return rejectedCount; }
        public void setRejectedCount(int rejectedCount) { this.rejectedCount = rejectedCount; }

        public int getTotalCount() {
            return pendingCount + approvedCount + rejectedCount;
        }
    }
}
