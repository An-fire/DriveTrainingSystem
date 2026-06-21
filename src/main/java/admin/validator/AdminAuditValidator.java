package admin.validator;

import admin.dto.AuditRequest;
import admin.dto.BatchAuditRequest;
import common.entity.Enrollment;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员审核验证器
 *
 * <p>提供审核相关业务规则验证，确保审核操作的合法性和数据完整性。</p>
 *
 * <h3>验证规则</h3>
 * <ul>
 *   <li>报名ID不能为空</li>
 *   <li>审核状态只能是 approved 或 rejected</li>
 *   <li>报名必须存在</li>
 *   <li>报名必须处于待审核状态</li>
 *   <li>批量审核时至少选择一个报名</li>
 * </ul>
 *
 * @author SuperDriver Team
 * @version 1.0
 */
public class AdminAuditValidator {

    // ==================== 验证常量 ====================

    /** 最大批量审核数量限制 */
    public static final int MAX_BATCH_SIZE = 100;

    /** 审核备注最大长度 */
    public static final int MAX_REMARK_LENGTH = 500;

    // ==================== 审核请求验证 ====================

    /**
     * 验证单个审核请求
     *
     * @param request 审核请求
     * @return 验证结果列表，空列表表示验证通过
     */
    public static List<String> validate(AuditRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            errors.add("请求对象不能为空");
            return errors;
        }

        // 验证报名ID
        if (request.getEnrollmentId() == null || request.getEnrollmentId().trim().isEmpty()) {
            errors.add("报名ID不能为空");
        }

        // 验证审核状态
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            errors.add("审核状态不能为空");
        } else if (!isValidStatus(request.getStatus())) {
            errors.add("审核状态无效，仅支持 approved（通过）或 rejected（拒绝）");
        }

        // 验证备注长度
        if (request.getRemark() != null && request.getRemark().length() > MAX_REMARK_LENGTH) {
            errors.add("审核备注不能超过" + MAX_REMARK_LENGTH + "个字符");
        }

        return errors;
    }

    /**
     * 验证批量审核请求
     *
     * @param request 批量审核请求
     * @return 验证结果列表，空列表表示验证通过
     */
    public static List<String> validate(BatchAuditRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            errors.add("请求对象不能为空");
            return errors;
        }

        // 验证ID列表
        if (request.getIds() == null || request.getIds().isEmpty()) {
            errors.add("请选择要审核的报名记录");
            return errors;
        }

        // 验证数量限制
        if (request.getIds().size() > MAX_BATCH_SIZE) {
            errors.add("批量审核数量不能超过" + MAX_BATCH_SIZE + "条");
        }

        // 验证审核状态
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            errors.add("审核状态不能为空");
        } else if (!isValidStatus(request.getStatus())) {
            errors.add("审核状态无效，仅支持 approved（通过）或 rejected（拒绝）");
        }

        // 验证备注长度
        if (request.getRemark() != null && request.getRemark().length() > MAX_REMARK_LENGTH) {
            errors.add("审核备注不能超过" + MAX_REMARK_LENGTH + "个字符");
        }

        return errors;
    }

    // ==================== 报名状态验证 ====================

    /**
     * 验证报名是否可以审核
     *
     * <p>检查报名是否存在且处于待审核状态。</p>
     *
     * @param enrollment 报名对象
     * @return 验证结果列表
     */
    public static List<String> validateEnrollmentForAudit(Enrollment enrollment) {
        List<String> errors = new ArrayList<>();

        if (enrollment == null) {
            errors.add("报名信息不存在");
            return errors;
        }

        if (!Enrollment.STATUS_PENDING.equals(enrollment.getStatus())) {
            errors.add("该报名已处理，当前状态为：" + getStatusText(enrollment.getStatus()));
        }

        return errors;
    }

    /**
     * 检查报名是否可审核
     *
     * @param enrollment 报名对象
     * @return true-可审核，false-不可审核
     */
    public static boolean isAuditable(Enrollment enrollment) {
        return enrollment != null && Enrollment.STATUS_PENDING.equals(enrollment.getStatus());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证状态值是否有效
     *
     * @param status 状态值
     * @return true-有效，false-无效
     */
    private static boolean isValidStatus(String status) {
        return Enrollment.STATUS_APPROVED.equals(status) || Enrollment.STATUS_REJECTED.equals(status);
    }

    /**
     * 获取状态文本
     *
     * @param status 状态值
     * @return 状态的中文描述
     */
    private static String getStatusText(String status) {
        return Enrollment.STATUS_TEXT_MAP.getOrDefault(status, "未知状态");
    }

    // ==================== 静态工具方法 ====================

    /**
     * 合并多个验证结果
     *
     * @param results 多个验证结果列表
     * @return 合并后的验证结果
     */
    public static List<String> merge(List<String>... results) {
        List<String> merged = new ArrayList<>();
        for (List<String> result : results) {
            if (result != null) {
                merged.addAll(result);
            }
        }
        return merged;
    }

    /**
     * 检查验证结果是否通过
     *
     * @param errors 验证错误列表
     * @return true-通过，false-未通过
     */
    public static boolean isValid(List<String> errors) {
        return errors == null || errors.isEmpty();
    }
}
