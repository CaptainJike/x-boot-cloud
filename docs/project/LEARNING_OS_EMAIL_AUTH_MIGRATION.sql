-- Learning OS 邮箱验证码登录/注册：已有数据库迁移脚本
-- 执行前请先确认没有重复的非空邮箱：
-- SELECT email, COUNT(*) FROM learner_account WHERE email IS NOT NULL AND email <> '' GROUP BY email HAVING COUNT(*) > 1;

UPDATE learner_account SET email = NULL WHERE email = '';

ALTER TABLE learner_account
    MODIFY `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
    MODIFY `github_user_id` varchar(64) DEFAULT NULL COMMENT 'GitHub 用户ID',
    MODIFY `github_login` varchar(255) DEFAULT NULL COMMENT 'GitHub 登录名',
    ADD UNIQUE KEY `uk_learner_account_email` (`email`);
