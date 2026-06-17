package student.service;

import common.database.BookingDAO;
import common.database.EnrollmentDAO;
import common.database.StaffDAO;
import common.database.UserDAO;
import common.entity.Booking;
import common.entity.Enrollment;
import common.entity.Staff;
import common.entity.User;
import common.util.MD5Util;
import common.util.UUIDUtil;
import student.exception.StudentException;
import student.util.StudentValidator;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class StudentService {
    private final UserDAO userDAO = new UserDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final StaffDAO staffDAO = new StaffDAO();

    /**
     * 学员注册
     */
    public int register(String name, String idCard, String phone, String password, String subject) {
        StudentValidator.checkName(name);
        StudentValidator.checkIdCard(idCard);
        StudentValidator.checkPhone(phone);
        StudentValidator.checkPwd(password);
        StudentValidator.checkSubject(subject);

        // 身份证查重
        if (userDAO.findByIdCard(idCard) != null) {
            throw new StudentException("该身份证已注册账号");
        }

        User user = new User();
        user.setId(UUIDUtil.getUUID());
        user.setName(name);
        user.setIdCard(idCard);
        user.setPhone(phone);
        user.setPassword(MD5Util.md5(password));
        user.setRole("student");
        user.setSubject(subject);
        user.setCreateTime(new Date());

        return userDAO.insert(user);
    }

    /**
     * 学员提交驾校报名
     */
    public int applyEnroll(String studentId, String coachId, String subjectType) {
        StudentValidator.checkSubject(subjectType);
        Enrollment enrollment = new Enrollment();
        enrollment.setId(UUIDUtil.getUUID());
        enrollment.setStudentId(studentId);
        enrollment.setCoachId(coachId);
        enrollment.setSubjectType(subjectType);
        enrollment.setStatus("pending");
        enrollment.setApplyTime(new Date());
        return enrollmentDAO.insert(enrollment);
    }

    /**
     * 查询当前学员报名记录
     */
    public Enrollment getMyEnroll(String studentId) {
        return enrollmentDAO.findByStudentId(studentId);
    }

    /**
     * 查询所有教练（供学员选择）
     */
    public List<Staff> listAllCoach() {
        return staffDAO.findAllCoaches();
    }

    /**
     * 学员新增练车预约
     */
    public int addBooking(String studentId, String coachId, String subjectType, Timestamp startTime, Timestamp endTime) {
        StudentValidator.checkTime(startTime, endTime);
        // 时间冲突校验
        boolean conflict = bookingDAO.checkTimeConflict(coachId, startTime, null);
        if (conflict) {
            throw new StudentException("该时段教练已有预约，请更换时间");
        }

        Booking booking = new Booking();
        booking.setId(UUIDUtil.getUUID());
        booking.setStudentId(studentId);
        booking.setCoachId(coachId);
        booking.setSubjectType(subjectType);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus("approved");
        booking.setStudentScore(null);
        booking.setCoachScore(null);
        booking.setCanExam(false);
        booking.setCreateTime(new Date());

        return bookingDAO.insert(booking);
    }

    /**
     * 查询学员个人所有预约
     */
    public List<Booking> listMyBooking(String studentId) {
        return bookingDAO.findByStudentId(studentId);
    }
}