-- 敏感词种子
INSERT INTO sensitive_words (word, category, severity) VALUES
('包治', 'medical', 2),
('根治', 'medical', 2),
('100% 有效', 'medical', 2),
('治愈率', 'medical', 2),
('加微信', 'ad', 2),
('加 vx', 'ad', 2),
('私信', 'ad', 2),
('团购', 'ad', 2),
('最便宜', 'ad', 2),
('全网最低', 'ad', 2),
('唯一', 'ad', 1),
('独家', 'ad', 1),
('第一', 'ad', 1),
('代购', 'ad', 2);