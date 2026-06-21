-- 2. 重建所有表（ID 统一为 VARCHAR(36)，适配 MySQL 原生 UUID()）
-- 学员表 user
CREATE TABLE user (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    name VARCHAR(50) NOT NULL COMMENT '学员姓名',
    idCard VARCHAR(18) UNIQUE NOT NULL COMMENT '身份证号（唯一）',
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号（唯一）',
    password VARCHAR(64) NOT NULL COMMENT '密码（建议用MD5加密）',
    role VARCHAR(20) DEFAULT 'student' COMMENT '角色：学员',
    subject VARCHAR(10) COMMENT '报考科目：C2/C3',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 工作人员表 staff（管理员、教练）
CREATE TABLE staff (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    idCard VARCHAR(18) COMMENT '身份证号',
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号（唯一）',
    password VARCHAR(64) NOT NULL COMMENT '密码（MD5加密）',
    role VARCHAR(20) NOT NULL COMMENT '角色：admin管理员/coach教练',
    subject VARCHAR(10) COMMENT '教练负责科目，管理员为空',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 学员报名表 enrollment
CREATE TABLE enrollment (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    studentId VARCHAR(36) NOT NULL COMMENT '学员ID',
    coachId VARCHAR(36) NOT NULL COMMENT '教练ID',
    subjectType VARCHAR(10) COMMENT '报考科目',
    status VARCHAR(20) COMMENT '状态：pending待审核/approved通过/rejected拒绝',
    applyTime DATETIME COMMENT '申请时间',
    auditTime DATETIME COMMENT '审核时间',
    auditRemark VARCHAR(500) COMMENT '审核备注',
    adminId VARCHAR(36) COMMENT '审核管理员ID',
    FOREIGN KEY (studentId) REFERENCES user(id),
    FOREIGN KEY (coachId) REFERENCES staff(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 练车预约表 booking
CREATE TABLE booking (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID主键',
    studentId VARCHAR(36) NOT NULL COMMENT '学员ID',
    coachId VARCHAR(36) NOT NULL COMMENT '教练ID',
    subjectType VARCHAR(10) COMMENT '练车科目',
    startTime DATETIME NOT NULL COMMENT '练车开始时间',
    endTime DATETIME NOT NULL COMMENT '练车结束时间',
    status VARCHAR(20) COMMENT '状态：approved同意/rejected拒绝',
    studentScore INT COMMENT '学员给教练评分（1-5）',
    coachScore INT COMMENT '教练给学员评分（1-5）',
    canExam BOOLEAN DEFAULT FALSE COMMENT '是否允许参加考试',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (studentId) REFERENCES user(id),
    FOREIGN KEY (coachId) REFERENCES staff(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 插入初始管理员和教练账号（密码统一为 123456）
INSERT INTO staff (id, name, phone, password, role, subject)
VALUES
(UUID(), '系统管理员', '13800000000', 'e10adc3949ba59abbe56e057f20f883e', 'admin', NULL),
(UUID(), '张教练', '13912345678', 'e10adc3949ba59abbe56e057f20f883e', 'coach', 'C2'),
(UUID(), '李教练', '13987654321', 'e10adc3949ba59abbe56e057f20f883e', 'coach', 'C3');

