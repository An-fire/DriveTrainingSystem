package coach.util;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 教练模块参数校验工具类
 * 配合 coach.html 前端使用
 */
public class CoachValidator {

    // 手机号正则（11位，以1开头）
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");

    // 科目类型常量
    public static final String[] SUBJECT_TYPES = {"C1", "C2", "C3", "C4", "C5", "C6", "D", "E", "F"};

    /**
     * 校验教练ID是否有效（UUID格式）
     */
    public static boolean isValidCoachId(String coachId) {
        if (coachId == null || coachId.trim().isEmpty()) {
            return false;
        }
        String id = coachId.trim();
        // 1) 标准UUID格式（带连字符）
        if (id.length() == 36 && id.indexOf('-') != -1) {
            try {
                UUID.fromString(id);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        // 2) 无连字符格式（32位十六进制）
        if (id.length() == 32 && id.matches("[0-9a-fA-F]+")) {
            return true;
        }
        return false;
    }

    public static boolean isValidBookingId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return false;
        }
        String id = bookingId.trim();
        if (id.length() == 36 && id.indexOf('-') != -1) {
            try {
                UUID.fromString(id);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        if (id.length() == 32 && id.matches("[0-9a-fA-F]+")) {
            return true;
        }
        return false;
    }

    /**
     * 校验评分是否在 1-5 之间
     */
    public static boolean isValidScore(Integer score) {
        return score != null && score >= 1 && score <= 5;
    }

    /**
     * 校验评分是否在 1-5 之间（字符串版本）
     */
    public static boolean isValidScore(String scoreStr) {
        if (scoreStr == null || scoreStr.trim().isEmpty()) {
            return false;
        }
        try {
            int score = Integer.parseInt(scoreStr.trim());
            return score >= 1 && score <= 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 校验考试资格标识是否有效
     */
    public static boolean isValidCanExam(Boolean canExam) {
        return canExam != null;
    }

    /**
     * 校验手机号是否有效
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * 校验科目类型是否有效
     */
    public static boolean isValidSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return false;
        }
        String trimmed = subject.trim();
        for (String valid : SUBJECT_TYPES) {
            if (valid.equals(trimmed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 校验字符串是否非空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 获取校验失败的错误信息
     */
    public static String getErrorMessage(String fieldName) {
        return "参数[" + fieldName + "]不合法";
    }

    /**
     * ============================================================
     * 综合校验方法（一次性校验多个参数）
     * ============================================================
     */

    /**
     * 综合校验：教练ID + 预约ID + 评分 + 考试资格
     * 用于 ScoreServlet 打分接口
     *
     * @param coachId   教练ID
     * @param bookingId 预约ID
     * @param score     评分
     * @param canExam   考试资格
     * @return 校验通过返回 null，失败返回错误信息
     */
    public static String validateScoreRequest(String coachId, String bookingId, Integer score, Boolean canExam) {
        if (!isValidCoachId(coachId)) {
            return "教练ID不合法";
        }
        if (!isValidBookingId(bookingId)) {
            return "预约ID不合法";
        }
        if (!isValidScore(score)) {
            return "评分必须在1-5之间";
        }
        if (!isValidCanExam(canExam)) {
            return "考试资格参数无效";
        }
        return null;
    }

    /**
     * 综合校验：教练ID + 预约ID + 评分（简化版）
     * 用于前端不一定传 canExam 的场景
     */
    public static String validateScoreRequest(String coachId, String bookingId, Integer score) {
        if (!isValidCoachId(coachId)) {
            return "教练ID不合法";
        }
        if (!isValidBookingId(bookingId)) {
            return "预约ID不合法";
        }
        if (!isValidScore(score)) {
            return "评分必须在1-5之间";
        }
        return null;
    }

    /**
     * 综合校验：教练ID（用于 PendingServlet、CommentServlet）
     */
    public static String validateCoachId(String coachId) {
        if (!isValidCoachId(coachId)) {
            return "教练ID不合法";
        }
        return null;
    }

    /**
     * 综合校验：预约ID（用于打分）
     */
    public static String validateBookingId(String bookingId) {
        if (!isValidBookingId(bookingId)) {
            return "预约ID不合法";
        }
        return null;
    }

    /**
     * 综合校验：评分（用于打分）
     */
    public static String validateScore(Integer score) {
        if (!isValidScore(score)) {
            return "评分必须在1-5之间";
        }
        return null;
    }

    /**
     * 综合校验：手机号（用于登录/注册）
     */
    public static String validatePhone(String phone) {
        if (!isValidPhone(phone)) {
            return "手机号格式不正确（需为11位数字，以1开头）";
        }
        return null;
    }

    /**
     * 综合校验：科目类型
     */
    public static String validateSubject(String subject) {
        if (!isValidSubject(subject)) {
            return "科目类型无效，请选择 C1/C2/C3/C4/C5/C6/D/E/F";
        }
        return null;
    }
}