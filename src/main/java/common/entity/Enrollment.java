package common.entity;

import lombok.Data;
import java.util.Date;

/**
 * 学员报名实体类
 *
 * <p>记录学员的报名信息，包括报名科目、审核状态、审核时间等。
 * 报名流程：pending（待审核） -> approved（已通过）/ rejected（已拒绝）
 * 审核通过后，学员的 enrollStatus 也会同步更新为对应状态。</p>
 *
 * @author SuperDriver Team
 * @version 1.0
 */
@Data
public class Enrollment {

    /**
     * 报名记录唯一标识
     */
    private String id;

    /**
     * 学员ID，关联User表的id字段
     */
    private String studentId;

    /**
     * 教练ID，关联Staff表的id字段
     */
    private String coachId;

    /**
     * 报考科目类型，支持：C1（手动挡）、C2（自动挡）、C3
     */
    private String subjectType;

    /**
     * 报名状态
     * <ul>
     *   <li>pending - 待审核</li>
     *   <li>approved - 已通过</li>
     *   <li>rejected - 已拒绝</li>
     * </ul>
     */
    private String status;

    /**
     * 报名申请时间
     */
    private Date applyTime;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 审核备注/意见
     */
    private String auditRemark;

    /**
     * 审核员ID，关联Staff表的id字段（审核该报名的管理员ID）
     */
    private String adminId;

    /**
     * 审核员名称（冗余字段，方便前端展示）
     */
    private String adminName;

    // ============ 辅助字段（用于关联查询，不对应数据库表）============

    /**
     * 学员姓名（用于关联查询后展示）
     */
    private String studentName;

    /**
     * 教练姓名（用于关联查询后展示）
     */
    private String coachName;

    /**
     * 状态常量定义
     */
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";

    /**
     * 状态文本映射（用于前端展示）
     */
    public static final java.util.Map<String, String> STATUS_TEXT_MAP = new java.util.LinkedHashMap<>();
    static {
        STATUS_TEXT_MAP.put(STATUS_PENDING, "待审核");
        STATUS_TEXT_MAP.put(STATUS_APPROVED, "已通过");
        STATUS_TEXT_MAP.put(STATUS_REJECTED, "已拒绝");
    }

    /**
     * 获取状态的显示文本
     * @return 状态对应的中文文本
     */
    public String getStatusText() {
        return STATUS_TEXT_MAP.getOrDefault(this.status, "未知状态");
    }

    /**
     * 检查报名是否可审核
     * @return true-可审核，false-不可审核
     */
    public boolean isAuditable() {
        return STATUS_PENDING.equals(this.status);
    }

    /**
     * 检查报名是否已审核
     * @return true-已审核，false-待审核
     */
    public boolean isAudited() {
        return STATUS_APPROVED.equals(this.status) || STATUS_REJECTED.equals(this.status);
    }
}
