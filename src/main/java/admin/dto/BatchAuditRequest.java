package admin.dto;

import java.util.List;

/**
 * 批量审核请求DTO
 *
 * <p>封装管理员提交批量审核请求的参数。</p>
 *
 * <h3>接口说明</h3>
 * <ul>
 *   <li>接口地址：POST /admin/audit?action=batch</li>
 *   <li>Content-Type: application/json</li>
 * </ul>
 *
 * <h3>请求示例</h3>
 * <pre>
 * POST /admin/audit?action=batch
 * Content-Type: application/json
 *
 * {
 *     "ids": ["id1", "id2", "id3"],
 *     "status": "approved",
 *     "remark": "材料齐全，予以通过"
 * }
 * </pre>
 *
 * @author SuperDriver Team
 * @version 1.0
 */
public class BatchAuditRequest {

    /**
     * 报名ID列表
     * <p>必填，待审核的报名ID集合</p>
     */
    private List<String> ids;

    /**
     * 审核状态
     * <p>必填，可选值：
     * <ul>
     *   <li>approved - 全部通过</li>
     *   <li>rejected - 全部拒绝</li>
     * </ul>
     */
    private String status;

    /**
     * 审核备注
     * <p>可选，对所有选中的报名记录生效</p>
     */
    private String remark;

    // ==================== 构造函数 ====================

    public BatchAuditRequest() {
    }

    public BatchAuditRequest(List<String> ids, String status, String remark) {
        this.ids = ids;
        this.status = status;
        this.remark = remark;
    }

    // ==================== Getter & Setter ====================

    /**
     * 获取报名ID列表
     *
     * @return 报名ID列表
     */
    public List<String> getIds() {
        return ids;
    }

    /**
     * 设置报名ID列表
     *
     * @param ids 报名ID列表
     */
    public void setIds(List<String> ids) {
        this.ids = ids;
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
     * 获取待审核数量
     *
     * @return ID列表的长度
     */
    public int getCount() {
        return ids != null ? ids.size() : 0;
    }

    /**
     * 检查是否为批量通过
     *
     * @return true-通过，false-拒绝
     */
    public boolean isApproved() {
        return "approved".equals(this.status);
    }

    /**
     * 检查是否为批量拒绝
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
        return "BatchAuditRequest{" +
                "ids=" + ids +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
