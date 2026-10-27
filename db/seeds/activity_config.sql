-- 活动配置种子数据
-- 限时活动截止时间（毫秒时间戳）：当前时间 + 7天
INSERT INTO system_configs (config_key, config_value, config_type, description, created_at, updated_at, deleted_at)
VALUES (
    'activity_countdown_end_time',
    UNIX_TIMESTAMP() * 1000 + 7 * 86400000,
    'number',
    '限时活动截止时间（毫秒时间戳）',
    NOW(),
    NOW(),
    NULL
) ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    updated_at = NOW();
