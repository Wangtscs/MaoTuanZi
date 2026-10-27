-- 系统配置
INSERT INTO system_configs (config_key, config_value, description, is_public) VALUES
('agreement.privacy.version', 'V2.0', '当前隐私政策版本', 1),
('agreement.service.version', 'V2.0', '当前用户协议版本', 1),
('disclaimer.medical.standard', '⚠️ 本内容仅供参考，不构成兽医诊断、医疗建议或治疗方案。若宠物出现持续不适、急性症状或疑似中毒，请立即前往正规宠物医院就诊。', '医疗免责声明标准版', 1),
('disclaimer.medical.push', '本提醒基于您设置的健康日程。如宠物不适，请咨询兽医。', '医疗免责声明推送版', 1),
('disclaimer.medical.emergency', '⚠️ 本提醒不能替代兽医诊断。如宠物出现以下情况请立即就医：\n• 持续呕吐 / 便血\n• 精神萎靡超过 24 小时\n• 拒食超过 24 小时\n• 任何急性症状', '医疗免责声明紧急版', 1),
('quota.free.pet_count', '2', '免费用户最多宠物数', 1),
('quota.free.reminder_count', '3', '免费用户最多提醒数', 1),
('quota.monthly.portrait.free', '1', '非会员每月免费画像次数', 1),
('vip.month.quota', '5', '月卡会员每月画像额度', 1),
('vip.quarter.quota', '8', '季卡会员每月画像额度', 1),
('vip.year.quota', '15', '年卡会员每月画像额度', 1),
('refund.portrait.hours', '24', '画像可退款时间（小时）', 0),
('refund.monthly_limit', '1', '每月退款次数限制', 0),
('ai.task.timeout_seconds', '60', 'AI 任务超时时间', 0),
('ai.task.max_retry', '3', 'AI 任务最大重试次数', 0);

-- 默认管理员（密码 admin123，BCrypt 加密）
INSERT INTO admin_users (username, password_hash, nickname, email, status) VALUES
('admin', '', '超级管理员', 'admin@maotuanzi.ai', 0);

-- 默认角色
INSERT INTO admin_roles (id, code, name, description, is_system) VALUES
(1, 'super_admin', '超级管理员', '拥有全部权限', 1),
(2, 'ops', '运营', '内容审核、用户管理', 1),
(3, 'finance', '财务', '订单、退款', 1),
(4, 'cs', '客服', '用户反馈', 1);

-- 权限
INSERT INTO admin_permissions (id, code, name, module) VALUES
(1, 'dashboard.view', '查看仪表盘', 'dashboard'),
(2, 'user.list', '查看用户', 'users'),
(3, 'user.detail', '查看用户详情', 'users'),
(4, 'user.ban', '封禁用户', 'users'),
(5, 'work.list', '查看作品', 'works'),
(6, 'work.review', '审核作品', 'works'),
(7, 'work.delete', '删除作品', 'works'),
(8, 'order.list', '查看订单', 'orders'),
(9, 'order.detail', '查看订单详情', 'orders'),
(10, 'refund.review', '审核退款', 'orders'),
(11, 'vip.config', '会员配置', 'vip'),
(12, 'vip.list', '会员列表', 'vip'),
(13, 'coupon.create', '创建卡券', 'coupons'),
(14, 'coupon.list', '卡券列表', 'coupons'),
(15, 'content.post.review', '审核帖子', 'content'),
(16, 'content.report.handle', '处理举报', 'content'),
(17, 'content.sensitive.manage', '敏感词管理', 'content'),
(18, 'stats.view', '查看统计', 'statistics'),
(19, 'settings.system', '系统设置', 'settings'),
(20, 'settings.config', '配置管理', 'settings'),
(21, 'admin.list', '管理员列表', 'admin'),
(22, 'admin.create', '创建管理员', 'admin'),
(23, 'admin.role.manage', '角色管理', 'admin'),
(24, 'log.view', '查看日志', 'admin');

-- 角色权限绑定（超级管理员全部）
INSERT INTO admin_role_permissions (role_id, permission_id)
SELECT 1, id FROM admin_permissions;

-- 运营权限
INSERT INTO admin_role_permissions (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4),
(2, 5), (2, 6), (2, 7),
(2, 15), (2, 16), (2, 17),
(2, 18);

-- 财务权限
INSERT INTO admin_role_permissions (role_id, permission_id) VALUES
(3, 1), (3, 8), (3, 9), (3, 10), (3, 18);

-- 客服权限
INSERT INTO admin_role_permissions (role_id, permission_id) VALUES
(4, 2), (4, 3),
(4, 15), (4, 16);

-- 管理员-角色
INSERT INTO admin_user_roles (admin_user_id, role_id) VALUES (1, 1);