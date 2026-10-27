-- 活动配置种子数据
INSERT INTO system_configs (config_key, config_value, description, is_public, created_at, updated_at)
VALUES ('activity_countdown_end_time', UNIX_TIMESTAMP() * 1000 + 7 * 86400000, '限时活动截止时间（毫秒时间戳）', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);
