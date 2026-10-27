-- ============================================================
-- 毛团子AI · 运维平台（Ops）模块
-- Flyway: V2__ops.sql
-- 说明：与业务运营后台（/admin/**）完全隔离，运维平台走 /ops/**
-- ============================================================

-- ------------------------------------------------------------
-- O1. ops_instances · 服务实例心跳
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ops_instances` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `instance_id` VARCHAR(64) NOT NULL COMMENT '实例唯一标识',
  `app_name` VARCHAR(64) NULL,
  `profile` VARCHAR(32) NULL COMMENT 'dev / prod',
  `host` VARCHAR(128) NULL,
  `ip` VARCHAR(64) NULL,
  `port` INT NULL,
  `pid` VARCHAR(32) NULL,
  `version` VARCHAR(32) NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'UP' COMMENT 'UP / DEGRADED / DOWN',
  `cpu_load` DOUBLE NULL COMMENT '进程 CPU 负载',
  `system_cpu_load` DOUBLE NULL,
  `heap_used_mb` BIGINT NULL,
  `heap_max_mb` BIGINT NULL,
  `heap_used_pct` DOUBLE NULL,
  `thread_count` INT NULL,
  `qps` DOUBLE NULL,
  `error_rate` DOUBLE NULL,
  `started_at` DATETIME NULL,
  `last_heartbeat_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_instance_id` (`instance_id`),
  KEY `idx_heartbeat` (`last_heartbeat_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维-服务实例';

-- ------------------------------------------------------------
-- O2. ops_metric_snapshots · 指标快照（趋势图数据源）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ops_metric_snapshots` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `instance_id` VARCHAR(64) NOT NULL,
  `metric_key` VARCHAR(64) NOT NULL COMMENT 'jvm.heap.used.pct / http.qps / http.error.rate ...',
  `metric_value` DOUBLE NOT NULL DEFAULT 0,
  `sample_time` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_lookup` (`metric_key`, `sample_time`),
  KEY `idx_instance_time` (`instance_id`, `sample_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维-指标快照';

-- ------------------------------------------------------------
-- O3. ops_alarm_rules · 告警规则
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ops_alarm_rules` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL,
  `metric_key` VARCHAR(64) NOT NULL,
  `operator` VARCHAR(8) NOT NULL DEFAULT 'gt' COMMENT 'gt / gte / lt / lte / eq',
  `threshold` DOUBLE NOT NULL DEFAULT 0,
  `duration_checks` INT NOT NULL DEFAULT 1 COMMENT '连续满足 N 次才告警',
  `level` VARCHAR(8) NOT NULL DEFAULT 'P2' COMMENT 'P0 / P1 / P2',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `silence_minutes` INT NOT NULL DEFAULT 0 COMMENT '告警静默期（分钟）',
  `channels` VARCHAR(128) NULL COMMENT '通知渠道：webhook/email/sms（逗号分隔，记录用）',
  `description` VARCHAR(256) NULL,
  `last_value` DOUBLE NULL,
  `last_check_at` DATETIME NULL,
  `firing` TINYINT(1) NOT NULL DEFAULT 0,
  `created_by` VARCHAR(64) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_metric` (`metric_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维-告警规则';

-- ------------------------------------------------------------
-- O4. ops_alarm_records · 告警记录
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ops_alarm_records` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `rule_id` BIGINT UNSIGNED NOT NULL,
  `rule_name` VARCHAR(128) NULL,
  `metric_key` VARCHAR(64) NULL,
  `metric_value` DOUBLE NULL,
  `threshold` DOUBLE NULL,
  `level` VARCHAR(8) NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'FIRING' COMMENT 'FIRING / RESOLVED',
  `message` VARCHAR(512) NULL,
  `fired_at` DATETIME NOT NULL,
  `resolved_at` DATETIME NULL,
  `duration_seconds` INT NULL,
  `ack_by` VARCHAR(64) NULL,
  `ack_at` DATETIME NULL,
  `remark` VARCHAR(256) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_time` (`status`, `fired_at`),
  KEY `idx_rule` (`rule_id`),
  KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维-告警记录';

-- ------------------------------------------------------------
-- O5. ops_jobs · 运维任务
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ops_jobs` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `job_code` VARCHAR(64) NOT NULL,
  `job_name` VARCHAR(128) NOT NULL,
  `cron` VARCHAR(64) NOT NULL COMMENT '6 位 Spring CronExpression：秒 分 时 日 月 周',
  `handler` VARCHAR(128) NULL COMMENT '内置处理器标识',
  `job_type` VARCHAR(16) NOT NULL DEFAULT 'builtin' COMMENT 'builtin / manual',
  `params` JSON NULL,
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `last_status` VARCHAR(16) NULL COMMENT 'SUCCESS / FAILED / RUNNING',
  `last_run_at` DATETIME NULL,
  `next_run_at` DATETIME NULL,
  `last_duration_ms` BIGINT NULL,
  `last_message` VARCHAR(512) NULL,
  `run_count` INT NOT NULL DEFAULT 0,
  `fail_count` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_code` (`job_code`),
  KEY `idx_enabled_next` (`enabled`, `next_run_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维-定时任务';

-- ------------------------------------------------------------
-- O6. ops_job_records · 任务执行记录
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ops_job_records` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `job_id` BIGINT UNSIGNED NOT NULL,
  `job_code` VARCHAR(64) NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING / SUCCESS / FAILED',
  `trigger_type` VARCHAR(16) NOT NULL DEFAULT 'AUTO' COMMENT 'AUTO / MANUAL',
  `instance_id` VARCHAR(64) NULL,
  `start_at` DATETIME NOT NULL,
  `end_at` DATETIME NULL,
  `duration_ms` BIGINT NULL,
  `message` TEXT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_job_time` (`job_id`, `start_at`),
  KEY `idx_status` (`status`, `start_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维-任务执行记录';

-- ------------------------------------------------------------
-- O7. 运维权限码（admin_permissions 追加）
-- ------------------------------------------------------------
INSERT IGNORE INTO admin_permissions (id, code, name, module) VALUES
(25, 'ops.view',       '查看运维总览', 'ops'),
(26, 'ops.metrics',    '查看实时监控', 'ops'),
(27, 'ops.system',     '查看系统资源', 'ops'),
(28, 'ops.log',        '查看运维日志', 'ops'),
(29, 'ops.alarm',      '告警管理',     'ops'),
(30, 'ops.job',        '任务调度',     'ops'),
(31, 'ops.datasource', '数据源管理',   'ops'),
(32, 'ops.storage',    '存储管理',     'ops'),
(33, 'ops.audit',      '审计日志',     'ops'),
(34, 'ops.config',     '运维配置',     'ops');

-- 运维角色（SRE）
INSERT IGNORE INTO admin_roles (id, code, name, description, is_system) VALUES
(5, 'sre', '运维工程师', '系统运维：监控、告警、任务、日志', 1);

-- 运维角色 → 运维权限
INSERT IGNORE INTO admin_role_permissions (role_id, permission_id) VALUES
(5, 1), (5, 25), (5, 26), (5, 27), (5, 28), (5, 29), (5, 30), (5, 31), (5, 32), (5, 33), (5, 34);

-- 超级管理员补齐运维权限
INSERT IGNORE INTO admin_role_permissions (role_id, permission_id) VALUES
(1, 25), (1, 26), (1, 27), (1, 28), (1, 29), (1, 30), (1, 31), (1, 32), (1, 33), (1, 34);

-- ------------------------------------------------------------
-- O8. 默认运维账号
--    admin / admin123  超级管理员
--    ops   / ops123    运维工程师（仅当不存在时创建）
-- ------------------------------------------------------------
UPDATE admin_users
SET password_hash = '$2a$10$ewGjK13DMFPH8iPP9WpiTuFUVyuefbRifoHDftMof4HpRqDzvmJIe'
WHERE username = 'admin' AND (password_hash IS NULL OR password_hash = '');

INSERT INTO admin_users (username, password_hash, nickname, email, status)
SELECT 'ops', '$2a$10$AiuuYRfZotmbBRhbVV4p8ewFt/hk8tVJTkYcIXnFINQ1HmNnfcqRe', '运维工程师', 'ops@maotuanzi.ai', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM admin_users WHERE username = 'ops');

INSERT IGNORE INTO admin_user_roles (admin_user_id, role_id)
SELECT u.id, 5 FROM admin_users u WHERE u.username = 'ops';

-- ------------------------------------------------------------
-- O9. 默认告警规则
-- ------------------------------------------------------------
INSERT IGNORE INTO ops_alarm_rules
(id, name, metric_key, operator, threshold, duration_checks, level, enabled, silence_minutes, channels, description) VALUES
(1, 'JVM 堆内存使用率过高',      'jvm.heap.used.pct',   'gt', 85,  3, 'P1', 1, 30, 'webhook', '堆内存使用率连续 3 次超过 85%'),
(2, '系统 CPU 负载过高',         'system.cpu.load',     'gt', 0.8, 3, 'P1', 1, 30, 'webhook', '进程 CPU 负载连续 3 次超过 80%'),
(3, '接口错误率异常',            'http.error.rate.5m',  'gt', 5,   2, 'P0', 1, 15, 'webhook', '近 5 分钟接口错误率超过 5%'),
(4, '接口 P95 延迟过高',         'http.p95.5m',         'gt', 1500,2, 'P2', 1, 30, 'webhook', '近 5 分钟 P95 延迟超过 1500ms'),
(5, '磁盘剩余空间不足',          'system.disk.free.pct','lt', 15,  1, 'P0', 1, 60, 'webhook', '磁盘剩余空间低于 15%'),
(6, '数据库连接池使用率过高',    'db.pool.used.pct',    'gt', 85,  2, 'P1', 1, 30, 'webhook', 'Hikari 连接池使用率超过 85%'),
(7, 'Redis 不可用',              'redis.available',     'lt', 1,   1, 'P0', 1, 10, 'webhook', 'Redis 连通性检查失败'),
(8, 'AI 任务失败率过高',         'ai.task.fail.rate.1h','gt', 20,  2, 'P2', 1, 60, 'webhook', '近 1 小时 AI 任务失败率超过 20%'),
(9, 'AI 任务积压',               'ai.task.pending.count','gt', 50, 2, 'P2', 0, 60, 'webhook', '待处理 AI 任务超过 50 个');

-- ------------------------------------------------------------
-- O10. 默认运维任务
--     cron 为 6 位（秒 分 时 日 月 周）
-- ------------------------------------------------------------
INSERT IGNORE INTO ops_jobs
(id, job_code, job_name, cron, handler, job_type, enabled, params) VALUES
(1,  'metrics.snapshot',    '指标快照采集',     '0 * * * * *',   'metricsSnapshot', 'builtin', 1, NULL),
(2,  'db.ping',             '数据库连通性检查', '0 */1 * * * *', 'dbPing',          'builtin', 1, NULL),
(3,  'redis.ping',          'Redis 连通性检查', '0 */1 * * * *', 'redisPing',       'builtin', 1, NULL),
(4,  'disk.check',          '磁盘空间检查',     '0 */10 * * * *','diskCheck',       'builtin', 1, NULL),
(5,  'ai.task.timeout',     'AI 任务超时扫描',  '0 */5 * * * *', 'aiTaskTimeout',   'builtin', 1, NULL),
(6,  'cleanup.idempotency', '清理过期幂等键',   '0 0 3 * * *',   'cleanupIdempotency','builtin',1, NULL),
(7,  'cleanup.snapshots',   '清理历史指标快照', '0 30 3 * * *',  'cleanupSnapshots','builtin', 1, '{"keepDays":7}'),
(8,  'cleanup.job.records', '清理任务执行记录', '0 40 3 * * *',  'cleanupJobRecords','builtin',1, '{"keepDays":30}'),
(9,  'cleanup.uploads',     '清理孤儿上传记录', '0 50 3 * * *',  'cleanupUploads',  'builtin', 0, '{"keepDays":30}'),
-- 注：alarm.evaluate 默认关闭，告警巡检由内置调度器（每 60s）驱动；
--     开启后可自定义频率，内置调度器有 30s 幂等保护，不会重复触发。
(10, 'alarm.evaluate',      '告警规则巡检',     '0 */1 * * * *', 'alarmEvaluate',   'builtin', 0, NULL);

-- ============================================================
-- 完毕
-- ============================================================
