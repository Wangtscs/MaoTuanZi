-- ============================================================
-- 毛团子AI · 一键建库脚本
-- 适用版本：V2.0 · 2026-08-19
-- 字符集：utf8mb4_unicode_ci
-- ============================================================

DROP DATABASE IF EXISTS `maotuanzi`;
CREATE DATABASE `maotuanzi` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `maotuanzi`;

-- ------------------------------------------------------------
-- 1. users · 用户表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `openid` VARCHAR(64) NOT NULL COMMENT '微信 OpenID',
  `unionid` VARCHAR(64) NULL COMMENT '微信 UnionID',
  `nickname` VARCHAR(64) NULL,
  `avatar_url` VARCHAR(512) NULL,
  `phone` VARCHAR(20) NULL COMMENT '脱敏存储',
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '0=未知 1=男 2=女',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=正常 1=封禁 2=注销',
  `vip_level` TINYINT NOT NULL DEFAULT 0 COMMENT '0=无 1=月 2=季 3=年',
  `vip_expire_at` DATETIME NULL,
  `register_source` VARCHAR(32) NULL,
  `last_login_at` DATETIME NULL,
  `agreement_version` VARCHAR(16) NULL COMMENT '协议版本',
  `agreement_accepted_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_vip_expire` (`vip_expire_at`, `vip_level`),
  KEY `idx_status_created` (`status`, `created_at`),
  KEY `idx_deleted` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. pets · 宠物档案表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `pets`;
CREATE TABLE `pets` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `nickname` VARCHAR(64) NOT NULL,
  `species` TINYINT NOT NULL COMMENT '1=猫 2=犬 3=其他',
  `breed_id` INT NULL,
  `breed_name` VARCHAR(64) NULL,
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '0=未知 1=公 2=母',
  `birthday` DATE NULL,
  `weight_kg` DECIMAL(5,2) NULL,
  `is_neutered` TINYINT(1) NOT NULL DEFAULT 0,
  `allergies` JSON NULL,
  `staple_food` VARCHAR(128) NULL,
  `avatar_url` VARCHAR(512) NULL,
  `remark` VARCHAR(256) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_active` (`user_id`, `deleted_at`),
  KEY `idx_user_species` (`user_id`, `species`, `deleted_at`),
  KEY `idx_breed` (`breed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物档案';

-- ------------------------------------------------------------
-- 3. breeds · 品种字典
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `breeds`;
CREATE TABLE `breeds` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `species` TINYINT NOT NULL COMMENT '1=猫 2=犬 3=其他',
  `name` VARCHAR(64) NOT NULL,
  `name_en` VARCHAR(64) NULL,
  `aliases` VARCHAR(128) NULL,
  `description` VARCHAR(512) NULL,
  `characteristics` VARCHAR(256) NULL,
  `care_tips` VARCHAR(512) NULL,
  `image_url` VARCHAR(512) NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_species_sort` (`species`, `sort_order`),
  KEY `idx_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='品种字典';

-- ------------------------------------------------------------
-- 4. ai_styles · AI 风格字典
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `ai_styles`;
CREATE TABLE `ai_styles` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `category` VARCHAR(32) NOT NULL COMMENT 'portrait_2d / portrait_3d / emoji / card / text',
  `name` VARCHAR(64) NOT NULL,
  `code` VARCHAR(64) NOT NULL COMMENT '业务代码',
  `description` VARCHAR(256) NULL,
  `preview_url` VARCHAR(512) NULL,
  `prompt_template` TEXT NULL,
  `is_vip_only` TINYINT(1) NOT NULL DEFAULT 0,
  `price_cents` INT NOT NULL DEFAULT 0 COMMENT '非会员单次价格（分）',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category`, `code`),
  KEY `idx_category_active` (`category`, `is_active`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 风格字典';

-- ------------------------------------------------------------
-- 5. ai_templates · 模板字典（证件卡/表情包）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `ai_templates`;
CREATE TABLE `ai_templates` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `category` VARCHAR(32) NOT NULL COMMENT 'card / emoji / journal',
  `name` VARCHAR(64) NOT NULL,
  `code` VARCHAR(64) NOT NULL,
  `preview_url` VARCHAR(512) NULL,
  `config` JSON NULL COMMENT '模板配置',
  `is_vip_only` TINYINT(1) NOT NULL DEFAULT 0,
  `price_cents` INT NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_code` (`category`, `code`),
  KEY `idx_category_active` (`category`, `is_active`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板字典';

-- ------------------------------------------------------------
-- 6. ai_tasks · AI 任务
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `ai_tasks`;
CREATE TABLE `ai_tasks` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `task_type` VARCHAR(32) NOT NULL COMMENT 'portrait_2d / portrait_3d / emoji / card / feeding / symptom / name',
  `style_id` INT NULL,
  `template_id` INT NULL,
  `pet_id` BIGINT UNSIGNED NULL,
  `input_image_url` VARCHAR(512) NULL,
  `extra_params` JSON NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=pending 1=processing 2=success 3=failed 4=cancelled',
  `retry_count` TINYINT NOT NULL DEFAULT 0,
  `error_code` INT NULL,
  `error_message` VARCHAR(512) NULL,
  `provider` VARCHAR(32) NULL,
  `provider_task_id` VARCHAR(128) NULL,
  `cost_cents` INT NOT NULL DEFAULT 0,
  `is_charged` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已扣费',
  `started_at` DATETIME NULL,
  `finished_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  KEY `idx_status` (`status`),
  KEY `idx_provider_task` (`provider`, `provider_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 任务';

-- ------------------------------------------------------------
-- 7. works · 作品
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `works`;
CREATE TABLE `works` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `pet_id` BIGINT UNSIGNED NULL,
  `task_id` BIGINT UNSIGNED NULL,
  `work_type` VARCHAR(32) NOT NULL COMMENT 'portrait_2d / portrait_3d / emoji / card',
  `title` VARCHAR(128) NULL,
  `preview_url` VARCHAR(512) NOT NULL,
  `result_url` VARCHAR(512) NULL COMMENT '高清图/3D 文件',
  `thumbnail_url` VARCHAR(512) NULL,
  `style_id` INT NULL,
  `template_id` INT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=生成中 1=预览 2=完成 3=已删',
  `is_public` TINYINT(1) NOT NULL DEFAULT 0,
  `like_count` INT NOT NULL DEFAULT 0,
  `view_count` INT NOT NULL DEFAULT 0,
  `preview_expire_at` DATETIME NULL,
  `extra` JSON NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  KEY `idx_user_pet` (`user_id`, `pet_id`),
  KEY `idx_public_created` (`is_public`, `created_at`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品';

-- ------------------------------------------------------------
-- 8. products · 商品
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` VARCHAR(64) NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  `description` VARCHAR(512) NULL,
  `product_type` TINYINT NOT NULL COMMENT '1=单次 2=订阅 3=套餐',
  `price_cents` INT NOT NULL,
  `original_price_cents` INT NULL,
  `duration_days` INT NULL COMMENT '订阅时长',
  `quota_count` INT NULL COMMENT '单次/会员额度',
  `quota_period` VARCHAR(16) NULL COMMENT 'monthly / once',
  `vip_level_required` TINYINT NULL,
  `image_url` VARCHAR(512) NULL,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type_active` (`product_type`, `is_active`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品';

-- ------------------------------------------------------------
-- 9. orders · 订单
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `product_id` VARCHAR(64) NOT NULL,
  `product_name` VARCHAR(128) NOT NULL,
  `product_type` TINYINT NOT NULL,
  `amount_cents` INT NOT NULL,
  `pay_amount_cents` INT NOT NULL,
  `discount_cents` INT NOT NULL DEFAULT 0,
  `coupon_id` BIGINT UNSIGNED NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待支付 1=已支付 2=已完成 3=已关闭 4=退款中 5=已退款 6=拒绝退款',
  `work_id` BIGINT UNSIGNED NULL COMMENT '关联作品（单次付费）',
  `wx_prepay_id` VARCHAR(128) NULL,
  `wx_transaction_id` VARCHAR(64) NULL,
  `paid_at` DATETIME NULL,
  `expire_at` DATETIME NULL,
  `closed_at` DATETIME NULL,
  `refunded_at` DATETIME NULL,
  `refund_amount_cents` INT NOT NULL DEFAULT 0,
  `client_ip` VARCHAR(64) NULL,
  `remark` VARCHAR(256) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status` (`status`),
  KEY `idx_wx_txn` (`wx_transaction_id`),
  KEY `idx_expire` (`expire_at`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单';

-- ------------------------------------------------------------
-- 10. vip_records · 会员流水
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `vip_records`;
CREATE TABLE `vip_records` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `order_id` BIGINT UNSIGNED NULL,
  `change_type` TINYINT NOT NULL COMMENT '1=开通 2=续费 3=升级 4=到期 5=退款撤销',
  `vip_level_before` TINYINT NOT NULL DEFAULT 0,
  `vip_level_after` TINYINT NOT NULL DEFAULT 0,
  `expire_before` DATETIME NULL,
  `expire_after` DATETIME NULL,
  `duration_days` INT NULL,
  `amount_cents` INT NOT NULL DEFAULT 0,
  `remark` VARCHAR(256) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员流水';

-- ------------------------------------------------------------
-- 11. refunds · 退款
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `refunds`;
CREATE TABLE `refunds` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `refund_no` VARCHAR(32) NOT NULL,
  `order_id` BIGINT UNSIGNED NOT NULL,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `amount_cents` INT NOT NULL,
  `reason` VARCHAR(256) NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待审核 1=退款中 2=审核拒绝 3=已退款',
  `wx_refund_id` VARCHAR(64) NULL,
  `review_remark` VARCHAR(256) NULL,
  `reviewed_by` VARCHAR(64) NULL,
  `reviewed_at` DATETIME NULL,
  `refunded_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order` (`order_id`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款';

-- ------------------------------------------------------------
-- 12. coupons · 卡券
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(32) NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  `type` TINYINT NOT NULL COMMENT '1=满减 2=折扣 3=免费券',
  `discount_cents` INT NULL,
  `discount_rate` DECIMAL(3,2) NULL,
  `min_amount_cents` INT NOT NULL DEFAULT 0,
  `valid_from` DATETIME NOT NULL,
  `valid_to` DATETIME NOT NULL,
  `total_count` INT NOT NULL DEFAULT 0,
  `remain_count` INT NOT NULL DEFAULT 0,
  `per_user_limit` INT NOT NULL DEFAULT 1,
  `scope` VARCHAR(64) NULL COMMENT '适用商品，空=全部',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_active_valid` (`is_active`, `valid_from`, `valid_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡券';

-- ------------------------------------------------------------
-- 13. coupon_claims · 卡券领取
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `coupon_claims`;
CREATE TABLE `coupon_claims` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `coupon_id` BIGINT UNSIGNED NOT NULL,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `order_id` BIGINT UNSIGNED NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=未使用 1=已使用 2=已过期',
  `claimed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `used_at` DATETIME NULL,
  `expire_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_coupon` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡券领取';

-- ------------------------------------------------------------
-- 14. health_reminders · 健康提醒
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `health_reminders`;
CREATE TABLE `health_reminders` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `pet_id` BIGINT UNSIGNED NOT NULL,
  `type` TINYINT NOT NULL COMMENT '1=疫苗 2=驱虫 3=体检 4=洗澡 5=美容 6=其他',
  `title` VARCHAR(128) NOT NULL,
  `cycle_days` INT NULL COMMENT '周期天数，空=单次',
  `next_run_at` DATETIME NOT NULL,
  `last_run_at` DATETIME NULL,
  `note` VARCHAR(256) NULL,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_pet` (`user_id`, `pet_id`, `deleted_at`),
  KEY `idx_active_next` (`is_active`, `next_run_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康提醒';

-- ------------------------------------------------------------
-- 15. weight_logs · 体重记录
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `weight_logs`;
CREATE TABLE `weight_logs` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `pet_id` BIGINT UNSIGNED NOT NULL,
  `weight_kg` DECIMAL(5,2) NOT NULL,
  `recorded_at` DATE NOT NULL,
  `note` VARCHAR(256) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_pet_date` (`user_id`, `pet_id`, `recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='体重记录';

-- ------------------------------------------------------------
-- 16. journal_entries · 时光手册
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `journal_entries`;
CREATE TABLE `journal_entries` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `pet_id` BIGINT UNSIGNED NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `content` TEXT NOT NULL,
  `images` JSON NULL,
  `mood` TINYINT NULL COMMENT '1=开心 2=日常 3=难过 4=生病',
  `entry_date` DATE NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_pet_date` (`user_id`, `pet_id`, `entry_date`, `deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时光手册';

-- ------------------------------------------------------------
-- 17. feedings · 喂养记录
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `feedings`;
CREATE TABLE `feedings` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `pet_id` BIGINT UNSIGNED NOT NULL,
  `fed_at` DATETIME NOT NULL,
  `food_type` VARCHAR(64) NULL,
  `amount_g` INT NULL,
  `note` VARCHAR(256) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_pet_fed` (`user_id`, `pet_id`, `fed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='喂养记录';

-- ------------------------------------------------------------
-- 18. feeding_plans · 喂养方案
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `feeding_plans`;
CREATE TABLE `feeding_plans` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `pet_id` BIGINT UNSIGNED NOT NULL,
  `order_id` BIGINT UNSIGNED NULL,
  `plan_content` JSON NOT NULL,
  `adjust_count` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_pet` (`user_id`, `pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='喂养方案';

-- ------------------------------------------------------------
-- 19. square_posts · 广场帖子
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `square_posts`;
CREATE TABLE `square_posts` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `work_id` BIGINT UNSIGNED NULL,
  `pet_id` BIGINT UNSIGNED NULL,
  `content` VARCHAR(512) NOT NULL,
  `image_urls` JSON NOT NULL,
  `topic` VARCHAR(32) NULL,
  `is_3d` TINYINT(1) NOT NULL DEFAULT 0,
  `like_count` INT NOT NULL DEFAULT 0,
  `view_count` INT NOT NULL DEFAULT 0,
  `comment_count` INT NOT NULL DEFAULT 0,
  `share_count` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=审核中 1=已发布 2=已下架 3=已删除',
  `audit_remark` VARCHAR(256) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  KEY `idx_status_created` (`status`, `created_at`),
  KEY `idx_topic_created` (`topic`, `status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广场帖子';

-- ------------------------------------------------------------
-- 20. post_likes · 点赞
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `post_likes`;
CREATE TABLE `post_likes` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT UNSIGNED NOT NULL,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞';

-- ------------------------------------------------------------
-- 21. post_comments · 评论
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `post_comments`;
CREATE TABLE `post_comments` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT UNSIGNED NOT NULL,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `parent_id` BIGINT UNSIGNED NULL,
  `content` VARCHAR(512) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0=审核中 1=正常 2=下架',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_post_created` (`post_id`, `created_at`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论';

-- ------------------------------------------------------------
-- 22. post_reports · 举报
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `post_reports`;
CREATE TABLE `post_reports` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT UNSIGNED NOT NULL,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `type` TINYINT NOT NULL COMMENT '1=色情 2=暴力 3=商业 4=侵权 5=其他',
  `reason` VARCHAR(256) NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待处理 1=已处理',
  `handled_by` VARCHAR(64) NULL,
  `handled_remark` VARCHAR(256) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `handled_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_post` (`post_id`),
  KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报';

-- ------------------------------------------------------------
-- 23. checkins · 打卡
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `checkins`;
CREATE TABLE `checkins` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `pet_id` BIGINT UNSIGNED NULL,
  `topic` VARCHAR(64) NOT NULL COMMENT '打卡主题',
  `content` VARCHAR(512) NULL,
  `image_urls` JSON NULL,
  `checkin_date` DATE NOT NULL,
  `consecutive_days` INT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_topic_date` (`user_id`, `topic`, `checkin_date`),
  KEY `idx_topic_date` (`topic`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡';

-- ------------------------------------------------------------
-- 24. notifications · 通知
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `type` VARCHAR(32) NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `content` VARCHAR(512) NULL,
  `payload` JSON NULL,
  `is_read` TINYINT(1) NOT NULL DEFAULT 0,
  `read_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read_created` (`user_id`, `is_read`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知';

-- ------------------------------------------------------------
-- 25. audit_logs · 审核日志
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `audit_logs`;
CREATE TABLE `audit_logs` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NULL,
  `content_type` VARCHAR(32) NOT NULL,
  `content_id` VARCHAR(64) NULL,
  `check_type` VARCHAR(32) NOT NULL COMMENT 'machine / manual / report',
  `check_result` VARCHAR(16) NOT NULL COMMENT 'pass / block / report',
  `reason` VARCHAR(256) NULL,
  `operator` VARCHAR(64) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_content` (`content_type`, `content_id`),
  KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核日志';

-- ------------------------------------------------------------
-- 26. sensitive_words · 敏感词
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `sensitive_words`;
CREATE TABLE `sensitive_words` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `word` VARCHAR(64) NOT NULL,
  `category` VARCHAR(32) NOT NULL COMMENT 'political / porn / violence / ad / medical',
  `severity` TINYINT NOT NULL DEFAULT 1 COMMENT '1=提示 2=拒绝',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`),
  KEY `idx_active_category` (`is_active`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词';

-- ------------------------------------------------------------
-- 27. uploads · 上传文件
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `uploads`;
CREATE TABLE `uploads` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `oss_key` VARCHAR(512) NOT NULL,
  `url` VARCHAR(512) NOT NULL,
  `file_name` VARCHAR(256) NULL,
  `content_type` VARCHAR(64) NULL,
  `size_bytes` BIGINT NOT NULL DEFAULT 0,
  `biz_type` VARCHAR(32) NULL COMMENT 'portrait / work / avatar / post',
  `related_id` VARCHAR(64) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  KEY `idx_biz` (`biz_type`, `related_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上传文件';

-- ------------------------------------------------------------
-- 28. idempotency_keys · 幂等键
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `idempotency_keys`;
CREATE TABLE `idempotency_keys` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `idempotency_key` VARCHAR(128) NOT NULL,
  `user_id` BIGINT UNSIGNED NULL,
  `endpoint` VARCHAR(128) NOT NULL,
  `response` JSON NULL,
  `expires_at` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key` (`idempotency_key`),
  KEY `idx_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='幂等键';

-- ------------------------------------------------------------
-- 29. system_configs · 系统配置
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `system_configs`;
CREATE TABLE `system_configs` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(128) NOT NULL,
  `config_value` TEXT NOT NULL,
  `description` VARCHAR(256) NULL,
  `is_public` TINYINT(1) NOT NULL DEFAULT 0,
  `updated_by` VARCHAR(64) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置';

-- ------------------------------------------------------------
-- 30. admin_users · 管理员
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `admin_users`;
CREATE TABLE `admin_users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `password_hash` VARCHAR(128) NOT NULL,
  `nickname` VARCHAR(64) NULL,
  `avatar_url` VARCHAR(512) NULL,
  `email` VARCHAR(128) NULL,
  `phone` VARCHAR(20) NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=正常 1=停用',
  `last_login_at` DATETIME NULL,
  `last_login_ip` VARCHAR(64) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员';

-- ------------------------------------------------------------
-- 31. admin_roles · 角色
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `admin_roles`;
CREATE TABLE `admin_roles` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(64) NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `description` VARCHAR(256) NULL,
  `is_system` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色';

-- ------------------------------------------------------------
-- 32. admin_permissions · 权限
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `admin_permissions`;
CREATE TABLE `admin_permissions` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(64) NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `module` VARCHAR(32) NOT NULL,
  `description` VARCHAR(256) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限';

-- ------------------------------------------------------------
-- 33. admin_user_roles · 管理员-角色
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `admin_user_roles`;
CREATE TABLE `admin_user_roles` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `admin_user_id` BIGINT UNSIGNED NOT NULL,
  `role_id` INT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`admin_user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员-角色';

-- ------------------------------------------------------------
-- 34. admin_role_permissions · 角色-权限
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `admin_role_permissions`;
CREATE TABLE `admin_role_permissions` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `role_id` INT NOT NULL,
  `permission_id` INT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限';

-- ------------------------------------------------------------
-- 35. admin_logs · 管理操作日志
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `admin_logs`;
CREATE TABLE `admin_logs` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `admin_user_id` BIGINT UNSIGNED NOT NULL,
  `module` VARCHAR(32) NOT NULL,
  `action` VARCHAR(64) NOT NULL,
  `target_id` VARCHAR(64) NULL,
  `payload` JSON NULL,
  `ip` VARCHAR(64) NULL,
  `user_agent` VARCHAR(256) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin_created` (`admin_user_id`, `created_at`),
  KEY `idx_module_action` (`module`, `action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理操作日志';

-- ============================================================
-- 完毕
-- ============================================================