package admin.service;

import admin.service.AdminService.ValidationResult;
import admin.service.AdminService.AuditResult;
import admin.service.AdminService.AuditStatistics;
import common.database.EnrollmentDAO;
import common.database.NotificationDAO;
import common.database.UserDAO;
import common.entity.Enrollment;
import common.entity.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AdminService 单元测试
 */
class AdminServiceTest {

    @Mock
    private EnrollmentDAO enrollmentDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private NotificationDAO notificationDAO;

    private AdminService adminService;
    private Staff mockAuditor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminService = new AdminService(enrollmentDAO, userDAO, notificationDAO);
        mockAuditor = new Staff();
        mockAuditor.setId("admin-001");
        mockAuditor.setName("管理员");
        mockAuditor.setRole("admin");
    }

    // ==================== 报名信息验证测试 ====================

    @Test
    void testValidateEnrollment_Null() {
        ValidationResult result = adminService.validateEnrollment(null);
        assertFalse(result.isSuccess());
        assertEquals("报名信息不能为空", result.getMessage());
    }

    @Test
    void testValidateEnrollment_MissingStudentId() {
        Enrollment e = new Enrollment();
        e.setCoachId("coach-001");
        e.setSubjectType("C2");
        e.setStatus("pending");
        ValidationResult result = adminService.validateEnrollment(e);
        assertFalse(result.isSuccess());
        assertEquals("学员ID不能为空", result.getMessage());
    }

    @Test
    void testValidateEnrollment_MissingCoachId() {
        Enrollment e = new Enrollment();
        e.setStudentId("stu-001");
        e.setSubjectType("C2");
        e.setStatus("pending");
        ValidationResult result = adminService.validateEnrollment(e);
        assertFalse(result.isSuccess());
        assertEquals("教练ID不能为空", result.getMessage());
    }

    @Test
    void testValidateEnrollment_InvalidSubject() {
        Enrollment e = new Enrollment();
        e.setStudentId("stu-001");
        e.setCoachId("coach-001");
        e.setSubjectType("C4");
        e.setStatus("pending");
        ValidationResult result = adminService.validateEnrollment(e);
        assertFalse(result.isSuccess());
        assertEquals("科目类型无效，仅支持C1/C2/C3", result.getMessage());
    }

    @Test
    void testValidateEnrollment_Success() {
        Enrollment e = new Enrollment();
        e.setStudentId("stu-001");
        e.setCoachId("coach-001");
        e.setSubjectType("C2");
        e.setStatus("pending");
        ValidationResult result = adminService.validateEnrollment(e);
        assertTrue(result.isSuccess());
    }

    // ==================== 审核前置验证测试 ====================

    @Test
    void testValidateEnrollmentAuditable_NullId() {
        ValidationResult result = adminService.validateEnrollmentAuditable(null);
        assertFalse(result.isSuccess());
        assertEquals("报名ID不能为空", result.getMessage());
    }

    @Test
    void testValidateEnrollmentAuditable_NotFound() {
        when(enrollmentDAO.findById("non-exist")).thenReturn(null);
        ValidationResult result = adminService.validateEnrollmentAuditable("non-exist");
        assertFalse(result.isSuccess());
        assertEquals("报名信息不存在", result.getMessage());
    }

    @Test
    void testValidateEnrollmentAuditable_AlreadyProcessed() {
        Enrollment e = new Enrollment();
        e.setId("en-001");
        e.setStatus("approved");
        when(enrollmentDAO.findById("en-001")).thenReturn(e);
        ValidationResult result = adminService.validateEnrollmentAuditable("en-001");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("该报名已处理"));
    }

    @Test
    void testValidateEnrollmentAuditable_Success() {
        Enrollment e = new Enrollment();
        e.setId("en-001");
        e.setStatus("pending");
        when(enrollmentDAO.findById("en-001")).thenReturn(e);
        ValidationResult result = adminService.validateEnrollmentAuditable("en-001");
        assertTrue(result.isSuccess());
    }

    // ==================== 单个审核测试 ====================

    @Test
    void testAuditSingle_InvalidStatus() {
        Enrollment e = new Enrollment();
        e.setId("en-001");
        e.setStatus("pending");
        when(enrollmentDAO.findById("en-001")).thenReturn(e);
        AuditResult result = adminService.auditSingle("en-001", "invalid", null, mockAuditor);
        assertFalse(result.isSuccess());
        assertEquals("审核状态无效，仅支持 approved/rejected", result.getMessage());
    }

    @Test
    void testAuditSingle_NotAuditable() {
        Enrollment e = new Enrollment();
        e.setId("en-001");
        e.setStatus("approved");
        when(enrollmentDAO.findById("en-001")).thenReturn(e);
        AuditResult result = adminService.auditSingle("en-001", "approved", null, mockAuditor);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("该报名已处理"));
    }

    @Test
    void testAuditSingle_Success() {
        Enrollment e = new Enrollment();
        e.setId("en-001");
        e.setStudentId("stu-001");
        e.setStatus("pending");
        when(enrollmentDAO.findById("en-001")).thenReturn(e);
        when(enrollmentDAO.updateStatusWithRemark("en-001", "approved", "通过")).thenReturn(1);
        when(userDAO.findById("stu-001")).thenReturn(null); // 跳过通知

        AuditResult result = adminService.auditSingle("en-001", "approved", "通过", mockAuditor);
        assertTrue(result.isSuccess());
        assertEquals("审核完成", result.getMessage());
        assertEquals(1, result.getProcessedCount());
        verify(enrollmentDAO).updateStatusWithRemark("en-001", "approved", "通过");
    }

    // ==================== 批量审核测试 ====================

    @Test
    void testAuditBatch_EmptyList() {
        AuditResult result = adminService.auditBatch(Collections.emptyList(), "approved", null, mockAuditor);
        assertFalse(result.isSuccess());
        assertEquals("请选择要审核的报名记录", result.getMessage());
    }

    @Test
    void testAuditBatch_AllInvalid() {
        Enrollment e = new Enrollment();
        e.setId("en-001");
        e.setStatus("approved"); // 已处理
        when(enrollmentDAO.findById("en-001")).thenReturn(e);
        List<String> ids = Collections.singletonList("en-001");
        AuditResult result = adminService.auditBatch(ids, "approved", null, mockAuditor);
        assertFalse(result.isSuccess());
        assertEquals("所选报名记录均不可审核", result.getMessage());
    }

    @Test
    void testAuditBatch_PartialSuccess() {
        Enrollment e1 = new Enrollment();
        e1.setId("en-001");
        e1.setStatus("pending");
        Enrollment e2 = new Enrollment();
        e2.setId("en-002");
        e2.setStatus("approved"); // 已处理，跳过

        when(enrollmentDAO.findById("en-001")).thenReturn(e1);
        when(enrollmentDAO.findById("en-002")).thenReturn(e2);
        when(enrollmentDAO.batchUpdateStatus(Arrays.asList("en-001"), "approved", null)).thenReturn(1);

        List<String> ids = Arrays.asList("en-001", "en-002");
        AuditResult result = adminService.auditBatch(ids, "approved", null, mockAuditor);
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("成功1条，跳过1条"));
        assertEquals(1, result.getProcessedCount());
    }

    // ==================== 统计测试 ====================

    @Test
    void testAuditStatistics() {
        when(enrollmentDAO.countByStatus("pending")).thenReturn(5);
        when(enrollmentDAO.countByStatus("approved")).thenReturn(10);
        when(enrollmentDAO.countByStatus("rejected")).thenReturn(2);

        AuditStatistics stat = adminService.getAuditStatistics();
        assertEquals(5, stat.getPendingCount());
        assertEquals(10, stat.getApprovedCount());
        assertEquals(2, stat.getRejectedCount());
        assertEquals(17, stat.getTotalCount());
    }

    // ==================== 常量测试 ====================

    @Test
    void testStatusConstants() {
        assertEquals("pending", AdminService.STATUS_PENDING);
        assertEquals("approved", AdminService.STATUS_APPROVED);
        assertEquals("rejected", AdminService.STATUS_REJECTED);
    }
}
