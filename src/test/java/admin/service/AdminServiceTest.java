package admin.service;

import admin.dto.AuditRequest;
import admin.dto.BatchAuditRequest;
import admin.validator.AdminAuditValidator;
import common.database.EnrollmentDAO;
import common.database.NotificationDAO;
import common.database.UserDAO;
import common.entity.Enrollment;
import common.entity.Staff;
import common.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// 使用内部类的完全限定名
import admin.service.AdminService.ValidationResult;
import admin.service.AdminService.AuditResult;
import admin.service.AdminService.AuditStatistics;

/**
 * AdminService 单元测试
 *
 * <p>测试管理员服务类的核心功能，包括：
 * <ul>
 *   <li>报名信息验证</li>
 *   <li>单个审核操作</li>
 *   <li>批量审核操作</li>
 *   <li>审核统计</li>
 *   <li>状态常量和辅助方法</li>
 * </ul>
 *
 * @author SuperDriver Team
 * @version 1.1
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

        // 创建模拟管理员
        mockAuditor = new Staff();
        mockAuditor.setId("admin-001");
        mockAuditor.setName("管理员");
        mockAuditor.setRole("admin");
    }

    // ==================== 报名信息验证测试 ====================

    @Nested
    @DisplayName("报名信息验证测试")
    class ValidateEnrollmentTests {

        @Test
        @DisplayName("验证失败：报名信息为空")
        void testValidateEnrollment_Null() {
            ValidationResult result = adminService.validateEnrollment(null);
            assertFalse(result.isSuccess());
            assertEquals("报名信息不能为空", result.getMessage());
        }

        @Test
        @DisplayName("验证失败：缺少学员ID")
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
        @DisplayName("验证失败：缺少教练ID")
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
        @DisplayName("验证失败：无效的科目类型")
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
        @DisplayName("验证成功：完整信息")
        void testValidateEnrollment_Success() {
            Enrollment e = new Enrollment();
            e.setStudentId("stu-001");
            e.setCoachId("coach-001");
            e.setSubjectType("C2");
            e.setStatus("pending");
            ValidationResult result = adminService.validateEnrollment(e);
            assertTrue(result.isSuccess());
        }
    }

    // ==================== 审核前置验证测试 ====================

    @Nested
    @DisplayName("审核前置验证测试")
    class ValidateEnrollmentAuditableTests {

        @Test
        @DisplayName("验证失败：报名ID为空")
        void testValidateEnrollmentAuditable_NullId() {
            ValidationResult result = adminService.validateEnrollmentAuditable(null);
            assertFalse(result.isSuccess());
            assertEquals("报名ID不能为空", result.getMessage());
        }

        @Test
        @DisplayName("验证失败：报名不存在")
        void testValidateEnrollmentAuditable_NotFound() {
            when(enrollmentDAO.findById("non-exist")).thenReturn(null);
            ValidationResult result = adminService.validateEnrollmentAuditable("non-exist");
            assertFalse(result.isSuccess());
            assertEquals("报名信息不存在", result.getMessage());
        }

        @Test
        @DisplayName("验证失败：报名已处理")
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
        @DisplayName("验证成功：报名可审核")
        void testValidateEnrollmentAuditable_Success() {
            Enrollment e = new Enrollment();
            e.setId("en-001");
            e.setStatus("pending");
            when(enrollmentDAO.findById("en-001")).thenReturn(e);
            ValidationResult result = adminService.validateEnrollmentAuditable("en-001");
            assertTrue(result.isSuccess());
        }
    }

    // ==================== 单个审核测试 ====================

    @Nested
    @DisplayName("单个审核测试")
    class AuditSingleTests {

        @Test
        @DisplayName("审核失败：无效状态")
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
        @DisplayName("审核失败：报名不可审核")
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
        @DisplayName("审核成功：审核通过")
        void testAuditSingle_ApproveSuccess() {
            Enrollment e = new Enrollment();
            e.setId("en-001");
            e.setStudentId("stu-001");
            e.setStatus("pending");
            when(enrollmentDAO.findById("en-001")).thenReturn(e);
            when(enrollmentDAO.updateStatusWithAdmin(eq("en-001"), eq("approved"), eq("通过"), eq("admin-001"))).thenReturn(1);

            AuditResult result = adminService.auditSingle("en-001", "approved", "通过", mockAuditor);
            assertTrue(result.isSuccess());
            assertEquals("审核通过", result.getMessage());
            assertEquals(1, result.getProcessedCount());

            // 验证调用了带adminId的方法
            verify(enrollmentDAO).updateStatusWithAdmin("en-001", "approved", "通过", "admin-001");
        }

        @Test
        @DisplayName("审核成功：审核拒绝")
        void testAuditSingle_RejectSuccess() {
            Enrollment e = new Enrollment();
            e.setId("en-001");
            e.setStudentId("stu-001");
            e.setStatus("pending");
            when(enrollmentDAO.findById("en-001")).thenReturn(e);
            when(enrollmentDAO.updateStatusWithAdmin(eq("en-001"), eq("rejected"), eq("材料不全"), eq("admin-001"))).thenReturn(1);

            AuditResult result = adminService.auditSingle("en-001", "rejected", "材料不全", mockAuditor);
            assertTrue(result.isSuccess());
            assertEquals("审核已拒绝", result.getMessage());
            assertEquals(1, result.getProcessedCount());
        }

        @Test
        @DisplayName("审核成功：同步更新学员状态")
        void testAuditSingle_UpdatesStudentEnrollmentStatus() {
            Enrollment e = new Enrollment();
            e.setId("en-001");
            e.setStudentId("stu-001");
            e.setStatus("pending");
            when(enrollmentDAO.findById("en-001")).thenReturn(e);
            when(enrollmentDAO.updateStatusWithAdmin(anyString(), anyString(), any(), any())).thenReturn(1);

            User mockUser = new User();
            mockUser.setId("stu-001");
            mockUser.setEnrollStatus("pending");
            when(userDAO.findById("stu-001")).thenReturn(mockUser);

            adminService.auditSingle("en-001", "approved", null, mockAuditor);

            // 验证学员状态被更新
            verify(userDAO).update(any(User.class));
        }
    }

    // ==================== 批量审核测试 ====================

    @Nested
    @DisplayName("批量审核测试")
    class AuditBatchTests {

        @Test
        @DisplayName("审核失败：空列表")
        void testAuditBatch_EmptyList() {
            AuditResult result = adminService.auditBatch(Collections.emptyList(), "approved", null, mockAuditor);
            assertFalse(result.isSuccess());
            assertEquals("请选择要审核的报名记录", result.getMessage());
        }

        @Test
        @DisplayName("审核失败：空ID列表")
        void testAuditBatch_NullList() {
            AuditResult result = adminService.auditBatch(null, "approved", null, mockAuditor);
            assertFalse(result.isSuccess());
            assertEquals("请选择要审核的报名记录", result.getMessage());
        }

        @Test
        @DisplayName("审核失败：所有报名都不可审核")
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
        @DisplayName("审核成功：部分成功")
        void testAuditBatch_PartialSuccess() {
            Enrollment e1 = new Enrollment();
            e1.setId("en-001");
            e1.setStudentId("stu-001");
            e1.setStatus("pending");
            Enrollment e2 = new Enrollment();
            e2.setId("en-002");
            e2.setStatus("approved"); // 已处理，跳过

            when(enrollmentDAO.findById("en-001")).thenReturn(e1);
            when(enrollmentDAO.findById("en-002")).thenReturn(e2);
            when(enrollmentDAO.batchUpdateStatusWithAdmin(anyList(), eq("approved"), eq(null), eq("admin-001"))).thenReturn(1);

            List<String> ids = Arrays.asList("en-001", "en-002");
            AuditResult result = adminService.auditBatch(ids, "approved", null, mockAuditor);
            assertTrue(result.isSuccess());
            assertTrue(result.getMessage().contains("成功1条，跳过1条"));
            assertEquals(1, result.getProcessedCount());
        }

        @Test
        @DisplayName("审核成功：全部通过")
        void testAuditBatch_AllSuccess() {
            Enrollment e1 = new Enrollment();
            e1.setId("en-001");
            e1.setStudentId("stu-001");
            e1.setStatus("pending");
            Enrollment e2 = new Enrollment();
            e2.setId("en-002");
            e2.setStudentId("stu-002");
            e2.setStatus("pending");

            when(enrollmentDAO.findById("en-001")).thenReturn(e1);
            when(enrollmentDAO.findById("en-002")).thenReturn(e2);
            when(enrollmentDAO.batchUpdateStatusWithAdmin(anyList(), eq("approved"), anyString(), eq("admin-001"))).thenReturn(2);

            List<String> ids = Arrays.asList("en-001", "en-002");
            AuditResult result = adminService.auditBatch(ids, "approved", "统一审核", mockAuditor);
            assertTrue(result.isSuccess());
            assertEquals("批量审核通过完成", result.getMessage());
            assertEquals(2, result.getProcessedCount());
        }
    }

    // ==================== 审核统计测试 ====================

    @Nested
    @DisplayName("审核统计测试")
    class AuditStatisticsTests {

        @Test
        @DisplayName("获取统计数据")
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

        @Test
        @DisplayName("统计数据为空")
        void testAuditStatistics_Empty() {
            when(enrollmentDAO.countByStatus(anyString())).thenReturn(0);

            AuditStatistics stat = adminService.getAuditStatistics();
            assertEquals(0, stat.getPendingCount());
            assertEquals(0, stat.getApprovedCount());
            assertEquals(0, stat.getRejectedCount());
            assertEquals(0, stat.getTotalCount());
        }
    }

    // ==================== 查询方法测试 ====================

    @Nested
    @DisplayName("查询方法测试")
    class QueryTests {

        @Test
        @DisplayName("获取待审核列表")
        void testGetPendingEnrollments() {
            Enrollment e1 = new Enrollment();
            e1.setId("en-001");
            when(enrollmentDAO.findAllPending()).thenReturn(Arrays.asList(e1));

            List<Enrollment> result = adminService.getPendingEnrollments();
            assertEquals(1, result.size());
            assertEquals("en-001", result.get(0).getId());
        }

        @Test
        @DisplayName("获取全部报名列表")
        void testGetAllEnrollments() {
            Enrollment e1 = new Enrollment();
            e1.setId("en-001");
            Enrollment e2 = new Enrollment();
            e2.setId("en-002");
            when(enrollmentDAO.findAll()).thenReturn(Arrays.asList(e1, e2));

            List<Enrollment> result = adminService.getAllEnrollments();
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("根据ID获取报名")
        void testGetEnrollmentById() {
            Enrollment e = new Enrollment();
            e.setId("en-001");
            when(enrollmentDAO.findById("en-001")).thenReturn(e);

            Enrollment result = adminService.getEnrollmentById("en-001");
            assertNotNull(result);
            assertEquals("en-001", result.getId());
        }
    }

    // ==================== 常量和内部类测试 ====================

    @Nested
    @DisplayName("常量和内部类测试")
    class ConstantsAndInnerClassTests {

        @Test
        @DisplayName("状态常量正确")
        void testStatusConstants() {
            assertEquals("pending", AdminService.STATUS_PENDING);
            assertEquals("approved", AdminService.STATUS_APPROVED);
            assertEquals("rejected", AdminService.STATUS_REJECTED);
        }

        @Test
        @DisplayName("ValidationResult成功创建")
        void testValidationResult_Success() {
            ValidationResult result = ValidationResult.success();
            assertTrue(result.isSuccess());
            assertNull(result.getMessage());
        }

        @Test
        @DisplayName("ValidationResult失败创建")
        void testValidationResult_Fail() {
            ValidationResult result = ValidationResult.fail("测试错误");
            assertFalse(result.isSuccess());
            assertEquals("测试错误", result.getMessage());
        }

        @Test
        @DisplayName("AuditResult成功创建")
        void testAuditResult_Success() {
            AuditResult result = AuditResult.success("成功消息", 5);
            assertTrue(result.isSuccess());
            assertEquals("成功消息", result.getMessage());
            assertEquals(5, result.getProcessedCount());
        }

        @Test
        @DisplayName("AuditResult失败创建")
        void testAuditResult_Fail() {
            AuditResult result = AuditResult.fail("失败消息");
            assertFalse(result.isSuccess());
            assertEquals("失败消息", result.getMessage());
            assertEquals(0, result.getProcessedCount());
        }
    }

    // ==================== DTO验证测试 ====================

    @Nested
    @DisplayName("DTO验证测试")
    class DTOValidationTests {

        @Test
        @DisplayName("AuditRequest验证成功")
        void testAuditRequest_Valid() {
            AuditRequest request = new AuditRequest("en-001", "approved", "通过");
            List<String> errors = AdminAuditValidator.validate(request);
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("AuditRequest验证失败：空状态")
        void testAuditRequest_InvalidStatus() {
            AuditRequest request = new AuditRequest("en-001", "invalid", null);
            List<String> errors = AdminAuditValidator.validate(request);
            assertFalse(errors.isEmpty());
        }

        @Test
        @DisplayName("BatchAuditRequest验证成功")
        void testBatchAuditRequest_Valid() {
            BatchAuditRequest request = new BatchAuditRequest(Arrays.asList("en-001", "en-002"), "approved", null);
            List<String> errors = AdminAuditValidator.validate(request);
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("BatchAuditRequest验证失败：空列表")
        void testBatchAuditRequest_EmptyList() {
            BatchAuditRequest request = new BatchAuditRequest(Collections.emptyList(), "approved", null);
            List<String> errors = AdminAuditValidator.validate(request);
            assertFalse(errors.isEmpty());
        }
    }
}
