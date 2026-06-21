package admin.dto;

/**
 * 审核请求DTO
 *
 * <p>封装管理员提交审核请求的参数，用于单个报名审核接口。</p>
 *
 * <h3>接口说明</h3>
 * <ul>
 *   <li>接口地址：POST /admin/audit?action=single</li>
 *   <li>Content-Type: application/x-www-form-urlencoded 或 application/json</li>
 * </ul>
 *
 * <h3>请求示例</h3>
 * <pre>
 * POST /admin/audit?action=single
 * Content-Type: application/json
 *
 * {
 *     "enrollmentId": "abc123",
 *     "status": "approved",
 *     "remark": "信息核对无误，予以通过"
 * }
 * </pre>
 *
 * @author SuperDriver Team
 * @version 1.0
 */
public class AuditRequest {

    /**
     * 报名记录ID
     * <p>必填，对应enrollment表的id字段</p>
     */
    private String enrollmentId;

    /**
     * 审核状态
     * <p>必填，可选值：
     * <ul>
     *   <li>approved - 审核通过</li>
     *   <li>rejected - 审核拒绝</li>
     * </ul>
     */
    private String status;

    /**
     * 审核备注/意见
     * <p>可选，用于记录审核意见或拒绝原因</p>
     */
    private String remark;

    // ==================== 构造函数 ====================

    public AuditRequest() {
    }

    public AuditRequest(String enrollmentId, String status, String remark) {
        this.enrollmentId = enrollmentId;
        this.status = status;
        this.remark = remark;
    }

    // ==================== Getter & Setter ====================

    /**
     * 获取报名ID
     *
     * @return 报名ID
     */
    public String getEnrollmentId() {
        return enrollmentId;
    }

    /**
     * 设置报名ID
     *
     * @param enrollmentId 报名ID
     */
    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    /**
     * 获取审核状态
     *
     * @return 审核状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置审核状态
     *
     * @param status 审核状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取审核备注
     *
     * @return 审核备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置审核备注
     *
     * @param remark 审核备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    // ==================== 辅助方法 ====================

    /**
     * 检查是否为审核通过
     *
     * @return true-通过，false-拒绝
     */
    public boolean isApproved() {
        return "approved".equals(this.status);
    }

    /**
     * 检查是否为审核拒绝
     *
     * @return true-拒绝，false-通过
     */
    public boolean isRejected() {
        return "rejected".equals(this.status);
    }

    /**
     * 获取状态描述文本
     *
     * @return 状态的中文描述
     */
    public String getStatusText() {
        if ("approved".equals(this.status)) {
            return "通过";
        } else if ("rejected".equals(this.status)) {
            return "拒绝";
        }
        return "未知";
    }

    @Override
    public String toString() {
        return "AuditRequest{" +
                "enrollmentId='" + enrollmentId + '\'' +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
