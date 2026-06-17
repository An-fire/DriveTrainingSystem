package student.util;

import student.exception.StudentException;

public class StudentValidator {
    // 姓名校验
    public static void checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new StudentException("姓名不能为空");
        }
    }

    // 身份证校验（18位）
    public static void checkIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            throw new StudentException("身份证必须为18位");
        }
    }

    // 手机号校验（11位纯数字）
    public static void checkPhone(String phone) {
        if (phone == null || !phone.matches("\\d{11}")) {
            throw new StudentException("手机号格式错误，请输入11位数字");
        }
    }

    // 密码基础校验
    public static void checkPwd(String pwd) {
        if (pwd == null || pwd.length() < 6) {
            throw new StudentException("密码长度不能少于6位");
        }
    }

    // 报考/练科目校验
    public static void checkSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new StudentException("驾照类型不能为空");
        }
        if (!"C1".equals(subject) && !"C2".equals(subject) && !"C3".equals(subject)) {
            throw new StudentException("仅支持 C1 / C2 / C3 驾照类型");
        }
    }

    // 预约时间校验
    public static void checkTime(java.sql.Timestamp startTime, java.sql.Timestamp endTime) {
        if (startTime == null || endTime == null) {
            throw new StudentException("预约时间不能为空");
        }
        if (startTime.after(endTime)) {
            throw new StudentException("开始时间不能晚于结束时间");
        }
    }
}