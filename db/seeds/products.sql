-- 商品种子数据
INSERT INTO products (id, name, description, product_type, price_cents, original_price_cents, duration_days, quota_count, quota_period, sort_order, is_active) VALUES
('portrait_single', '高清宠物画像', '解锁 2D 宠物画像高清无水印版本', 1, 990, 1990, NULL, 1, 'once', 100, 1),
('portrait_3d_single', '3D 萌宠数字手办', '单图生成 3D 可互动手办', 1, 1990, 3990, NULL, 1, 'once', 110, 1),
('feeding_single', '专属喂养方案', '基于宠物档案生成专业喂养方案', 1, 1990, 3990, NULL, 1, 'once', 120, 1),
('breed_manual', '品种深度养护手册', '品种专属深度养护指南', 1, 990, 1990, NULL, 1, 'once', 130, 1),
('emoji_pack', '表情包合集', '萌宠表情包 9 张', 1, 990, 1990, NULL, 1, 'once', 140, 1),
('id_card', '萌宠证件卡', '个性化萌宠证件卡', 1, 990, 1990, NULL, 1, 'once', 150, 1),
('vip_month', '月卡会员', '每月 5 张高清画像额度 + 会员专属风格', 2, 1990, 2990, 30, 5, 'monthly', 200, 1),
('vip_quarter', '季卡会员', '每月 8 张高清画像额度 + 会员专属', 2, 4990, 7990, 90, 24, 'monthly', 210, 1),
('vip_year', '年卡会员', '每月 15 张高清画像额度 + 全部会员权益', 2, 12900, 19900, 365, 180, 'monthly', 220, 1),
('starter_pack', '新人体验包', '3 张画像 + 1 张 3D + 表情包', 3, 2990, 5990, 365, 5, 'once', 300, 1),
('family_pack', '全家福套餐', '10 张画像 + 2 个 3D', 3, 8990, 15990, 365, 12, 'once', 310, 1);

-- AI 风格种子
INSERT INTO ai_styles (id, category, name, code, description, is_vip_only, price_cents, sort_order, is_active) VALUES
(1, 'portrait_2d', '油画风格', 'oil_painting', '经典油画质感', 0, 990, 10, 1),
(2, 'portrait_2d', '迪士尼卡通', 'disney', '迪士尼可爱卡通', 0, 990, 20, 1),
(3, 'portrait_2d', '水彩', 'watercolor', '清新水彩手绘', 0, 990, 30, 1),
(4, 'portrait_2d', '赛博朋克', 'cyberpunk', '霓虹未来感', 1, 1990, 40, 1),
(5, 'portrait_2d', '国风水墨', 'chinese_ink', '传统水墨意境', 1, 1990, 50, 1),
(6, 'portrait_2d', '日系动漫', 'anime', '二次元萌系', 1, 1990, 60, 1),
(7, 'portrait_2d', '梵高星空', 'vangogh', '后印象派', 1, 1990, 70, 1),
(8, 'portrait_2d', '吉卜力', 'ghibli', '宫崎骏动画风', 1, 1990, 80, 1),
(9, 'portrait_3d', '可爱手办', 'chibi_3d', 'Q 版 3D 萌宠', 0, 1990, 10, 1),
(10, 'portrait_3d', '写实渲染', 'realistic_3d', '超写实 3D 模型', 1, 3990, 20, 1);