-- 更新管理员李紫嫣的USB令牌（密码和USB令牌都是123456的MD5）
UPDATE staff 
SET usbToken = 'e10adc3949ba59abbe56e057f20f883e' 
WHERE phone = '15478523654' AND role = 'admin';

-- 验证更新结果
SELECT id, name, phone, role, usbToken FROM staff WHERE role = 'admin';