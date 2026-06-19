-- ============================================
-- 驾校练车信息管理系统 - 数据库初始化脚本
-- 先删除旧表（按依赖顺序），再创建新表
-- ============================================

-- 禁用外键检查，避免删除时因外键约束报错
SET FOREIGN_KEY_CHECKS = 0;

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS enrollment;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS user;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 1. 学员表 user
-- ============================================
CREATE TABLE user (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    name VARCHAR(50) NOT NULL COMMENT '学员姓名',
    idCard VARCHAR(18) UNIQUE NOT NULL COMMENT '身份证号（唯一）',
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号（唯一）',
    password VARCHAR(64) NOT NULL COMMENT '密码（MD5加密）',
    role VARCHAR(20) DEFAULT 'student' COMMENT '角色：学员',
    subject VARCHAR(10) COMMENT '报考科目：C1/C2/C3',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 2. 工作人员表 staff（管理员、教练）
-- ============================================
CREATE TABLE staff (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号（唯一）',
    password VARCHAR(64) NOT NULL COMMENT '密码（MD5加密）',
    role VARCHAR(20) NOT NULL COMMENT '角色：admin管理员/coach教练',
    subject VARCHAR(10) COMMENT '教练负责科目，管理员为空',
    usbToken VARCHAR(64) COMMENT 'USB安全令牌（管理员专用，MD5加密）',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 3. 学员报名表 enrollment
-- ============================================
CREATE TABLE enrollment (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    studentId VARCHAR(36) NOT NULL COMMENT '学员ID',
    coachId VARCHAR(36) NOT NULL COMMENT '教练ID',
    subjectType VARCHAR(10) COMMENT '报考科目',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending待审核/approved通过/rejected拒绝',
    applyTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    auditTime DATETIME COMMENT '审核时间',
    FOREIGN KEY (studentId) REFERENCES user(id),
    FOREIGN KEY (coachId) REFERENCES staff(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 4. 练车预约表 booking
-- ============================================
CREATE TABLE booking (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    studentId VARCHAR(36) NOT NULL COMMENT '学员ID',
    coachId VARCHAR(36) NOT NULL COMMENT '教练ID',
    subjectType VARCHAR(10) COMMENT '练车科目',
    startTime DATETIME NOT NULL COMMENT '练车开始时间',
    endTime DATETIME NOT NULL COMMENT '练车结束时间',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending待处理/approved同意/rejected拒绝',
    studentScore INT COMMENT '学员给教练评分（1-5）',
    coachScore INT COMMENT '教练给学员评分（1-5）',
    canExam BOOLEAN DEFAULT FALSE COMMENT '是否允许参加考试',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (studentId) REFERENCES user(id),
    FOREIGN KEY (coachId) REFERENCES staff(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 5. 插入初始管理员和教练账号（密码统一为 123456）
-- ============================================
INSERT INTO staff (id, name, phone, password, role, subject, usbToken)
VALUES
(UUID(), '系统管理员', '13800000000', 'e10adc3949ba59abbe56e057f20f883e', 'admin', NULL, 'e10adc3949ba59abbe56e057f20f883e'),
(UUID(), '张教练', '13912345678', 'e10adc3949ba59abbe56e057f20f883e', 'coach', 'C2', NULL),
(UUID(), '李教练', '13987654321', 'e10adc3949ba59abbe56e057f20f883e', 'coach', 'C3', NULL);

-- 查看插入结果
SELECT * FROM staff;
