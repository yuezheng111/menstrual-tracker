-- Migration V5: identity and display name separation

ALTER TABLE users
    ADD COLUMN nickname VARCHAR(50) NULL AFTER avatar,
    ADD COLUMN login_type VARCHAR(20) NOT NULL DEFAULT 'PASSWORD' AFTER role,
    ADD COLUMN password_change_required TINYINT(1) NOT NULL DEFAULT 0 AFTER enabled;

-- Existing WeChat users get a unique generated account name and a display name.
UPDATE users
SET username = CONCAT('wx_', LEFT(SHA2(open_id, 256), 12)),
    login_type = 'WECHAT',
    nickname = COALESCE(NULLIF(TRIM(nickname), ''), '微信用户')
WHERE open_id IS NOT NULL
  AND deleted = 0;

-- Existing admins must change their password after this upgrade.
UPDATE users
SET password_change_required = 1
WHERE role = 'ADMIN'
  AND deleted = 0;
