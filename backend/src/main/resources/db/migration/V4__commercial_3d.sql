-- ============================================================
-- 毛团子AI · 商业化（阶段3 3D 模型生成 / 阶段5 广场 3D 收口）
-- Flyway: V4__commercial_3d.sql
-- ============================================================

-- S1. square_posts.model_url · 3D 模型资产地址（is_3d=1 时承载）
ALTER TABLE square_posts ADD COLUMN IF NOT EXISTS `model_url` VARCHAR(512) NULL COMMENT '3D 模型 glTF 地址';

-- M1. model3d_tasks · 3D 生成任务
CREATE TABLE IF NOT EXISTS `model3d_tasks` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `task_id` VARCHAR(64) NOT NULL COMMENT '业务任务号（对外暴露，唯一）',
  `user_id` BIGINT UNSIGNED NOT NULL,
  `source_image_url` VARCHAR(512) NOT NULL COMMENT '输入宠物原图 URL',
  `style` VARCHAR(64) NULL COMMENT '3D 风格/参数',
  `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/SUCCEEDED/FAILED',
  `model_url` VARCHAR(512) NULL COMMENT '产出 glTF 地址',
  `oss_key` VARCHAR(512) NULL COMMENT 'OSS object key',
  `error_msg` VARCHAR(512) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_model3d_task_id` (`task_id`),
  KEY `idx_model3d_user` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='3D 模型生成任务';
