-- ============================================
-- 修复管理员 USB 令牌数据
-- 李紫嫣：usbTokenPlain = 123456
-- MD5(123456) = e10adc3949ba59abbe56e057f20f883e
-- ============================================

UPDATE staff 
SET 
    usbToken = 'e10adc3949ba59abbe56e057f20f883e',
    usbTokenPlain = '123456'
WHERE name = '李紫嫣' AND role = 'admin';

-- 验证更新结果
SELECT id, name, phone, role, usbToken, usbTokenPlain 
FROM staff 
WHERE role = 'admin';