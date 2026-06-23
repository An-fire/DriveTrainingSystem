package student.service;

import common.database.*;
import common.entity.Booking;
import common.entity.Enrollment;
import common.entity.Staff;
import common.entity.User;
import student.exception.StudentException;
import student.util.StudentValidator;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class StudentService {
    private final UserDAO userDAO = new UserDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final StaffDAO staffDAO = new StaffDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final CoachScheduleDAO coachScheduleDAO = new CoachScheduleDAO();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    // ====================== 1 学员注册（复用UserDAO.insert/findByPhone/findByIdCard） ======================
    public int register(String name, String idCard, String phone, String password, String subject) {
        // 参数校验
        StudentValidator.checkName(name);
        StudentValidator.checkIdCard(idCard);
        StudentValidator.checkPhone(phone);
        StudentValidator.checkPwd(password);
        StudentValidator.checkSubject(subject);

        // 校验手机号、身份证唯一，防止重复注册
        if (userDAO.findByPhone(phone) != null) {
            throw new StudentException("该手机号已注册");
        }
        if (userDAO.findByIdCard(idCard) != null) {
            throw new StudentException("该身份证已注册");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(name);
        user.setIdCard(idCard);
        user.setPhone(phone);
        user.setPassword(password);
        user.setRole("student");
        user.setSubject(subject);
        user.setCreateTime(new Timestamp(System.currentTimeMillis()));
        return userDAO.insert(user);
    }

    // ====================== 2 获取全部教练（复用StaffDAO.findAllCoaches） ======================
    public List<Staff> listAllCoach() {
        return staffDAO.findAllCoaches();
    }

    // ====================== 3 根据学员ID查询报名记录（复用EnrollmentDAO.findByStudentId） ======================
    public Enrollment getMyEnroll(String studentId) {
        return enrollmentDAO.findByStudentId(studentId);
    }

    // ====================== 4 提交驾校报考（复用EnrollmentDAO.insert） ======================
    public int applyEnroll(String studentId, String coachId, String subjectType) {
        StudentValidator.checkSubject(subjectType);
        // 校验该学员是否已有待审核报名
        Enrollment exist = enrollmentDAO.findByStudentId(studentId);
        if (exist != null && "pending".equals(exist.getStatus())) {
            throw new StudentException("你已有待审核的报考申请，不可重复提交");
        }

        // ===== 新增：校验教练是否匹配所选科目 =====
        Staff coach = staffDAO.findById(coachId);
        if (coach == null) {
            throw new StudentException("教练不存在，请重新选择");
        }
        if (!subjectType.equals(coach.getSubject())) {
            throw new StudentException("所选教练不教授 " + subjectType + " 科目，请选择匹配的教练");
        }

        Enrollment enroll = new Enrollment();
        enroll.setId(UUID.randomUUID().toString());
        enroll.setStudentId(studentId);
        enroll.setCoachId(coachId);
        enroll.setSubjectType(subjectType);
        enroll.setStatus("pending");
        enroll.setApplyTime(new Timestamp(System.currentTimeMillis()));
        enroll.setAuditTime(null);
        return enrollmentDAO.insert(enroll);
    }

    // ====================== 5 提交练车预约（复用BookingDAO insert/checkTimeConflict） ======================
    public int addBooking(String studentId, String coachId, String subjectType,
                          String startTime, String endTime) {
        StudentValidator.checkSubject(subjectType);

        Timestamp startTs;
        Timestamp endTs;
        try {
            startTs = new Timestamp(sdf.parse(startTime).getTime());
            endTs = new Timestamp(sdf.parse(endTime).getTime());
        } catch (Exception e) {
            throw new StudentException("时间格式错误，请使用 yyyy-MM-dd HH:mm");
        }
        StudentValidator.checkTime(startTs, endTs);
        if (startTs.before(new Timestamp(System.currentTimeMillis()))) {
            throw new StudentException("预约时间不能早于当前时间，请选择未来的空闲时段");
        }
        // ========== 校验学员报名状态 ==========
        User user = userDAO.findById(studentId);
        if (user == null) {
            throw new StudentException("学员信息不存在");
        }
        if (!"approved".equals(user.getEnrollStatus())) {
            throw new StudentException("您的报名尚未通过审核，无法预约练车，请等待管理员审核");
        }
        // 跨天校验
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = dateFormat.format(startTs);
        String endDate = dateFormat.format(endTs);
        if (!startDate.equals(endDate)) {
            throw new StudentException("预约不能跨天，请选择同一天的起止时间");
        }
        // 校验教练是否有排班
        if (!coachScheduleDAO.hasSchedule(coachId, startTs, endTs)) {
            throw new StudentException("该教练在此时间段未设置可预约时段，请选择教练排班时间");
        }
        // 校验教练时段冲突
        boolean hasConflict = bookingDAO.checkTimeConflict(coachId, startTs, endTs, null);
        if (hasConflict) {
            throw new StudentException("该教练此时间段已有预约，无法重复预约");
        }
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID().toString());
        booking.setStudentId(studentId);
        booking.setCoachId(coachId);
        booking.setSubjectType(subjectType);
        booking.setStartTime(startTs);
        booking.setEndTime(endTs);
        booking.setStatus("approved");
        booking.setStudentScore(null);
        booking.setCoachScore(null);
        booking.setCanExam(null);
        return bookingDAO.insert(booking);
    }

    // ====================== 6 查询我的全部预约（复用BookingDAO.findByStudentId） ======================
    public List<Booking> listMyBooking(String studentId) {
        return bookingDAO.findByStudentId(studentId);
    }

    // ====================== 7 取消预约（复用BookingDAO.updateStatus） ======================
    public void cancelBooking(String bookingId, String studentId) {
        List<Booking> myList = bookingDAO.findByStudentId(studentId);
        boolean isOwn = myList.stream().anyMatch(b -> b.getId().equals(bookingId));
        if (!isOwn) {
            throw new StudentException("无权操作他人预约记录");
        }
        int rows = bookingDAO.updateStatus(bookingId, "rejected");
        if (rows <= 0) {
            throw new StudentException("取消预约失败");
        }
    }

    // ====================== 8 校验时段冲突 ======================
    public boolean checkCoachTimeConflict(String coachId, String start, String end) {
        try {
            Timestamp s = new Timestamp(sdf.parse(start).getTime());
            Timestamp e = new Timestamp(sdf.parse(end).getTime());
            return bookingDAO.checkTimeConflict(coachId, s, e, null);
        } catch (Exception ex) {
            throw new StudentException("时间解析失败");
        }
    }


}