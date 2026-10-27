-- ============================================================
-- 毛团子AI · 商业化（阶段0 契约 / 阶段1 双支付 / 阶段4 分享奖励）
-- Flyway: V3__commercial.sql
-- ============================================================

-- O1. orders.channel · 支付渠道（WECHAT / HUAWEI）
ALTER TABLE orders ADD COLUMN IF NOT EXISTS `channel` VARCHAR(16) NOT NULL DEFAULT 'WECHAT' COMMENT '支付渠道 WECHAT/HUAWEI';

-- O2. users.invite_code / inviter_id · 邀请体系
ALTER TABLE users ADD COLUMN IF NOT EXISTS `invite_code` VARCHAR(32) NULL COMMENT '邀请码（唯一）';
ALTER TABLE users ADD COLUMN IF NOT EXISTS `inviter_id` BIGINT NULL COMMENT '邀请人用户 ID';
CREATE UNIQUE INDEX IF NOT EXISTS `uk_users_invite_code` ON `users`(`invite_code`);

-- O3. rewards · 分享/邀请奖励
CREATE TABLE IF NOT EXISTS `rewards` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(32) NOT NULL COMMENT 'VIP_DAYS / COUPON',
  `value` INT NOT NULL DEFAULT 0,
  `ref_type` VARCHAR(32) NULL COMMENT 'SHARE / INVITE / INVITE_INVITER',
  `ref_id` BIGINT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0 待领取 1 已领取 2 已过期',
  `remark` VARCHAR(255) NULL,
  `expire_at` DATETIME NULL,
  `claimed_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_rewards_user` (`user_id`),
  KEY `idx_rewards_ref` (`ref_type`, `ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享/邀请奖励';
