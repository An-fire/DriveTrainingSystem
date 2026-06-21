package admin.dto;

import java.util.Date;

/**
 * 审核响应DTO
 *
 * <p>封装审核操作的结果数据，用于前端展示。</p>
 *
 * <h3>响应示例</h3>
 * <pre>
 * // 成功响应
 * {
 *     "code": 1,
 *     "msg": "审核通过",
 *     "data": {
 *         "enrollmentId": "abc123",
 *         "status": "approved",
 *         "statusText": "通过",
 *         "auditorId": "admin001",
 *         "auditorName": "张三",
 *         "auditTime": "2026-06-21 16:30:00",
 *         "remark": "材料核实无误"
 *     }
 * }
 *
 * // 失败响应
 * {
 *     "code": 0,
 *     "msg": "该报名已处理，无法重复审核"
 * }
 * </pre>
 *
 * @author SuperDriver Team
 * @version 1.0
 */
public class AuditResponse {

    // ==================== 响应字段 ====================

    /**
     * 响应码
     * <ul>
     *   <li>1 - 成功</li>
     *   <li>0 - 失败</li>
     * </ul>
     */
    private int code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private AuditData data;

    // ==================== 内部类：审核数据 ====================

    /**
     * 审核数据详情
     */
    public static class AuditData {

        /**
         * 报名ID
         */
        private String enrollmentId;

        /**
         * 审核状态（英文）
         */
        private String status;

        /**
         * 审核状态（中文）
         */
        private String statusText;

        /**
         * 审核员ID
         */
        private String auditorId;

        /**
         * 审核员姓名
         */
        private String auditorName;

        /**
         * 审核时间
         */
        private Date auditTime;

        /**
         * 审核备注
         */
        private String remark;

        // ==================== 构造函数 ====================

        public AuditData() {
        }

        public AuditData(String enrollmentId, String status, String auditorId, String auditorName, Date auditTime, String remark) {
            this.enrollmentId = enrollmentId;
            this.status = status;
            this.statusText = formatStatusText(status);
            this.auditorId = auditorId;
            this.auditorName = auditorName;
            this.auditTime = auditTime;
            this.remark = remark;
        }

        // ==================== Getter & Setter ====================

        public String getEnrollmentId() {
            return enrollmentId;
        }

        public void setEnrollmentId(String enrollmentId) {
            this.enrollmentId = enrollmentId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
            this.statusText = formatStatusText(status);
        }

        public String getStatusText() {
            return statusText;
        }

        public void setStatusText(String statusText) {
            this.statusText = statusText;
        }

        public String getAuditorId() {
            return auditorId;
        }

        public void setAuditorId(String auditorId) {
            this.auditorId = auditorId;
        }

        public String getAuditorName() {
            return auditorName;
        }

        public void setAuditorName(String auditorName) {
            this.auditorName = auditorName;
        }

        public Date getAuditTime() {
            return auditTime;
        }

        public void setAuditTime(Date auditTime) {
            this.auditTime = auditTime;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        // ==================== 私有方法 ====================

        private static String formatStatusText(String status) {
            if ("approved".equals(status)) {
                return "已通过";
            } else if ("rejected".equals(status)) {
                return "已拒绝";
            }
            return "未知状态";
        }
    }

    // ==================== 构造函数 ====================

    public AuditResponse() {
    }

    /**
     * 创建成功响应
     *
     * @param data 审核数据
     * @return 成功响应对象
     */
    public static AuditResponse success(AuditData data) {
        AuditResponse response = new AuditResponse();
        response.setCode(1);
        response.setMsg("操作成功");
        response.setData(data);
        return response;
    }

    /**
     * 创建成功响应（带自定义消息）
     *
     * @param message 成功消息
     * @param data 审核数据
     * @return 成功响应对象
     */
    public static AuditResponse success(String message, AuditData data) {
        AuditResponse response = new AuditResponse();
        response.setCode(1);
        response.setMsg(message);
        response.setData(data);
        return response;
    }

    /**
     * 创建失败响应
     *
     * @param message 错误消息
     * @return 失败响应对象
     */
    public static AuditResponse fail(String message) {
        AuditResponse response = new AuditResponse();
        response.setCode(0);
        response.setMsg(message);
        return response;
    }

    // ==================== Getter & Setter ====================

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public AuditData getData() {
        return data;
    }

    public void setData(AuditData data) {
        this.data = data;
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return this.code == 1;
    }
}
