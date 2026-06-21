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
 * 管理员服务类
 *
 * <p>负责管理员后台的核心业务逻辑，包括：
 * <ul>
 *   <li>学员报名审核（单个/批量）</li>
 *   <li>待审核报名查询</li>
 *   <li>审核统计数据</li>
 *   <li>报名信息验证</li>
 * </ul>
 *
 * <h2>审核流程说明</h2>
 * <ol>
 *   <li>管理员查询待审核报名列表</li>
 *   <li>管理员对报名进行审核（通过/拒绝）</li>
 *   <li>系统记录审核结果、管理员ID、时间戳</li>
 *   <li>系统同步更新学员的报名状态</li>
 *   <li>系统向学员发送审核通知</li>
 * </ol>
 *
 * @author SuperDriver Team
 * @version 1.1
 */
public class AdminService {

    private final EnrollmentDAO enrollmentDAO;
    private final UserDAO userDAO;
    private final NotificationDAO notificationDAO;

    // ==================== 状态常量 ====================

    /** 待审核状态 */
    public static final String STATUS_PENDING = "pending";

    /** 已通过状态 */
    public static final String STATUS_APPROVED = "approved";

    /** 已拒绝状态 */
    public static final String STATUS_REJECTED = "rejected";

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     * 初始化DAO层依赖
     */
    public AdminService() {
        this.enrollmentDAO = new EnrollmentDAO();
        this.userDAO = new UserDAO();
        this.notificationDAO = new NotificationDAO();
    }

    /**
     * 测试用构造函数
     * 允许注入mock的DAO实现
     *
     * @param enrollmentDAO 报名DAO
     * @param userDAO 用户DAO
     * @param notificationDAO 通知DAO
     */
    public AdminService(EnrollmentDAO enrollmentDAO, UserDAO userDAO, NotificationDAO notificationDAO) {
        this.enrollmentDAO = enrollmentDAO;
        this.userDAO = userDAO;
        this.notificationDAO = notificationDAO;
    }

    // ==================== 核心业务方法 ====================

    /**
     * 验证报名信息完整性
     *
     * <p>检查报名记录的必填字段是否完整有效。</p>
     *
     * @param enrollment 报名信息
     * @return 验证结果，包含成功/失败状态和错误消息
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
     * 验证报名是否可以审核
     *
     * <p>检查报名是否存在且处于待审核状态。</p>
     *
     * @param enrollmentId 报名ID
     * @return 验证结果
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
     * 审核单个报名
     *
     * <p>执行报名审核操作，包括：
     * <ul>
     *   <li>验证报名可审核</li>
     *   <li>更新报名状态和审核信息</li>
     *   <li>同步更新学员的报名状态</li>
     *   <li>发送审核通知给学员</li>
     * </ul>
     *
     * @param enrollmentId 报名ID
     * @param status 审核状态（approved/rejected）
     * @param remark 审核备注
     * @param auditor 审核管理员
     * @return 审核结果
     * @throws IllegalArgumentException 如果状态不是approved或rejected
     */
    public AuditResult auditSingle(String enrollmentId, String status, String remark, Staff auditor) {
        // 参数验证
        ValidationResult v = validateEnrollmentAuditable(enrollmentId);
        if (!v.isSuccess()) {
            return AuditResult.fail(v.getMessage());
        }
        if (!STATUS_APPROVED.equals(status) && !STATUS_REJECTED.equals(status)) {
            return AuditResult.fail("审核状态无效，仅支持 approved/rejected");
        }

        // 获取报名信息用于后续处理
        Enrollment enrollment = enrollmentDAO.findById(enrollmentId);

        // 更新报名状态（记录审核管理员ID）
        int rows = enrollmentDAO.updateStatusWithAdmin(enrollmentId, status, remark, auditor.getId());
        if (rows <= 0) {
            return AuditResult.fail("审核操作失败");
        }

        // 同步更新学员的报名状态
        updateStudentEnrollmentStatus(enrollment.getStudentId(), status);

        // 发送审核通知
        notifyStudent(enrollmentId, status, remark);

        String message = STATUS_APPROVED.equals(status) ? "审核通过" : "审核已拒绝";
        return AuditResult.success(message, 1);
    }

    /**
     * 批量审核报名
     *
     * <p>批量执行报名审核操作：
     * <ul>
     *   <li>验证每个报名是否可审核</li>
     *   <li>只处理处于待审核状态的报名</li>
     *   <li>更新所有选中的报名状态</li>
     *   <li>同步更新每个学员的报名状态</li>
     *   <li>发送审核通知</li>
     * </ul>
     *
     * @param enrollmentIds 报名ID列表
     * @param status 审核状态
     * @param remark 审核备注（对所有选中记录生效）
     * @param auditor 审核管理员
     * @return 审核结果
     */
    public AuditResult auditBatch(List<String> enrollmentIds, String status, String remark, Staff auditor) {
        // 参数验证
        if (enrollmentIds == null || enrollmentIds.isEmpty()) {
            return AuditResult.fail("请选择要审核的报名记录");
        }
        if (!STATUS_APPROVED.equals(status) && !STATUS_REJECTED.equals(status)) {
            return AuditResult.fail("审核状态无效，仅支持 approved/rejected");
        }

        // 过滤出有效的报名ID
        int validCount = 0;
        List<String> validIds = new ArrayList<>();
        List<String> studentIds = new ArrayList<>();

        for (String id : enrollmentIds) {
            ValidationResult v = validateEnrollmentAuditable(id);
            if (v.isSuccess()) {
                validIds.add(id);
                Enrollment enrollment = enrollmentDAO.findById(id);
                if (enrollment != null) {
                    studentIds.add(enrollment.getStudentId());
                }
                validCount++;
            }
        }

        if (validIds.isEmpty()) {
            return AuditResult.fail("所选报名记录均不可审核");
        }

        // 批量更新报名状态
        int rows = enrollmentDAO.batchUpdateStatusWithAdmin(validIds, status, remark, auditor.getId());

        // 同步更新学员的报名状态
        for (String studentId : studentIds) {
            updateStudentEnrollmentStatus(studentId, status);
        }

        // 发送审核通知
        for (String id : validIds) {
            notifyStudent(id, status, remark);
        }

        // 生成结果消息
        String msg;
        if (validCount < enrollmentIds.size()) {
            msg = String.format("审核完成，成功%d条，跳过%d条（已处理）", validCount, enrollmentIds.size() - validCount);
        } else {
            msg = STATUS_APPROVED.equals(status) ? "批量审核通过完成" : "批量审核拒绝完成";
        }

        return AuditResult.success(msg, validCount);
    }

    // ==================== 查询方法 ====================

    /**
     * 获取待审核报名列表
     *
     * @return 待审核的报名列表，按申请时间升序
     */
    public List<Enrollment> getPendingEnrollments() {
        return enrollmentDAO.findAllPending();
    }

    /**
     * 获取全部报名列表
     *
     * @return 所有报名记录，按申请时间降序
     */
    public List<Enrollment> getAllEnrollments() {
        return enrollmentDAO.findAll();
    }

    /**
     * 根据ID获取报名详情
     *
     * @param enrollmentId 报名ID
     * @return 报名记录，不存在返回null
     */
    public Enrollment getEnrollmentById(String enrollmentId) {
        return enrollmentDAO.findById(enrollmentId);
    }

    /**
     * 获取审核统计数据
     *
     * @return 包含各状态数量的统计对象
     */
    public AuditStatistics getAuditStatistics() {
        AuditStatistics stat = new AuditStatistics();
        stat.setPendingCount(enrollmentDAO.countByStatus(STATUS_PENDING));
        stat.setApprovedCount(enrollmentDAO.countByStatus(STATUS_APPROVED));
        stat.setRejectedCount(enrollmentDAO.countByStatus(STATUS_REJECTED));
        return stat;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 同步更新学员的报名状态
     *
     * <p>当报名被审核通过或拒绝时，同步更新对应学员的enrollStatus字段。</p>
     *
     * @param studentId 学员ID
     * @param enrollmentStatus 报名状态
     */
    private void updateStudentEnrollmentStatus(String studentId, String enrollmentStatus) {
        if (isBlank(studentId)) {
            return;
        }
        try {
            User user = userDAO.findById(studentId);
            if (user != null) {
                user.setEnrollStatus(enrollmentStatus);
                userDAO.update(user);
            }
        } catch (Exception e) {
            // 记录错误但不影响主流程
            e.printStackTrace();
        }
    }

    /**
     * 发送审核通知给学员
     *
     * <p>创建系统通知，告知学员审核结果。</p>
     *
     * @param enrollmentId 报名ID
     * @param status 审核状态
     * @param remark 审核备注
     */
    private void notifyStudent(String enrollmentId, String status, String remark) {
        try {
            Enrollment enrollment = enrollmentDAO.findById(enrollmentId);
            if (enrollment == null) {
                return;
            }

            User user = userDAO.findById(enrollment.getStudentId());
            if (user == null) {
                return;
            }

            // 构建通知内容
            String content;
            if (STATUS_APPROVED.equals(status)) {
                content = "恭喜！您的报名申请已通过审核，现在可以预约练车了。" + formatRemark(remark);
            } else {
                content = "很抱歉，您的报名申请未通过审核。" + formatRemark(remark);
            }

            // 创建通知
            Notification notification = new Notification();
            notification.setId(UUIDUtil.getUUID());
            notification.setUserId(user.getId());
            notification.setType("enrollment_audit");
            notification.setContent(content);
            notification.setIsRead(0);
            notification.setCreateTime(new Date());

            notificationDAO.insert(notification);
        } catch (Exception e) {
            // 记录错误但不影响主流程
            e.printStackTrace();
        }
    }

    /**
     * 格式化审核备注
     *
     * @param remark 原始备注
     * @return 格式化后的备注文本
     */
    private String formatRemark(String remark) {
        if (isBlank(remark)) {
            return "";
        }
        return "（审核意见：" + remark + "）";
    }

    /**
     * 检查字符串是否为空
     *
     * @param str 待检查字符串
     * @return true-为空，false-非空
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    // ==================== 内部类：验证结果 ====================

    /**
     * 验证结果封装
     *
     * <p>用于封装业务验证的结果，包含成功/失败状态和错误消息。</p>
     */
    public static class ValidationResult {
        private final boolean success;
        private final String message;

        private ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        /**
         * 创建成功结果
         *
         * @return 成功状态的验证结果
         */
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        /**
         * 创建失败结果
         *
         * @param message 错误消息
         * @return 失败状态的验证结果
         */
        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }

        /**
         * 获取是否成功
         *
         * @return true-成功，false-失败
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * 获取错误消息
         *
         * @return 错误消息，成功时返回null
         */
        public String getMessage() {
            return message;
        }
    }

    // ==================== 内部类：审核结果 ====================

    /**
     * 审核结果封装
     *
     * <p>用于封装审核操作的结果，包含成功/失败状态、消息和处理数量。</p>
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

        /**
         * 创建成功结果
         *
         * @param message 成功消息
         * @param count 处理的数量
         * @return 成功状态的审核结果
         */
        public static AuditResult success(String message, int count) {
            return new AuditResult(true, message, count);
        }

        /**
         * 创建失败结果
         *
         * @param message 错误消息
         * @return 失败状态的审核结果
         */
        public static AuditResult fail(String message) {
            return new AuditResult(false, message, 0);
        }

        /**
         * 获取是否成功
         *
         * @return true-成功，false-失败
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * 获取结果消息
         *
         * @return 结果消息
         */
        public String getMessage() {
            return message;
        }

        /**
         * 获取处理的数量
         *
         * @return 处理数量
         */
        public int getProcessedCount() {
            return processedCount;
        }
    }

    // ==================== 内部类：审核统计 ====================

    /**
     * 审核统计信息
     *
     * <p>封装审核状态的统计数据。</p>
     */
    public static class AuditStatistics {
        private int pendingCount;
        private int approvedCount;
        private int rejectedCount;

        /**
         * 获取待审核数量
         *
         * @return 待审核数量
         */
        public int getPendingCount() {
            return pendingCount;
        }

        /**
         * 设置待审核数量
         *
         * @param pendingCount 待审核数量
         */
        public void setPendingCount(int pendingCount) {
            this.pendingCount = pendingCount;
        }

        /**
         * 获取已通过数量
         *
         * @return 已通过数量
         */
        public int getApprovedCount() {
            return approvedCount;
        }

        /**
         * 设置已通过数量
         *
         * @param approvedCount 已通过数量
         */
        public void setApprovedCount(int approvedCount) {
            this.approvedCount = approvedCount;
        }

        /**
         * 获取已拒绝数量
         *
         * @return 已拒绝数量
         */
        public int getRejectedCount() {
            return rejectedCount;
        }

        /**
         * 设置已拒绝数量
         *
         * @param rejectedCount 已拒绝数量
         */
        public void setRejectedCount(int rejectedCount) {
            this.rejectedCount = rejectedCount;
        }

        /**
         * 获取总数量
         *
         * @return 所有状态的总数量
         */
        public int getTotalCount() {
            return pendingCount + approvedCount + rejectedCount;
        }
    }
}
