# 毛团子AI · 数据库脚本

## 目录结构

```
db/
├── migrations/        # Flyway 迁移脚本（生产部署）
├── seeds/            # 种子数据（开发/测试）
└── schema.sql        # 一键建库脚本（开发期快速初始化）
```

## 快速开始

### 方式 A：一键建库（推荐本地开发）

```bash
mysql -u root -p < db/schema.sql
mysql -u root -p < db/seeds/breeds.sql
mysql -u root -p < db/seeds/products.sql
```

### 方式 B：Flyway 迁移（推荐生产部署）

后端启动时自动执行 `backend/src/main/resources/migration/`：

```sql
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 核心表

| 表 | 说明 |
|----|------|
| users | 用户表 |
| pets | 宠物档案 |
| breeds | 品种字典 |
| ai_tasks | AI 任务 |
| ai_styles | AI 风格字典 |
| ai_templates | 模板字典 |
| works | 作品 |
| orders | 订单 |
| products | 商品 |
| vip_records | 会员流水 |
| refunds | 退款 |
| coupons | 卡券 |
| coupon_claims | 卡券领取 |
| health_reminders | 健康提醒 |
| weight_logs | 体重记录 |
| journal_entries | 时光手册 |
| feedings | 喂养记录 |
| feeding_plans | 喂养方案 |
| square_posts | 广场帖子 |
| post_likes | 点赞 |
| post_comments | 评论 |
| post_reports | 举报 |
| checkins | 打卡 |
| notifications | 通知 |
| audit_logs | 审核日志 |
| sensitive_words | 敏感词 |
| uploads | 上传文件 |
| idempotency_keys | 幂等键 |
| system_configs | 系统配置 |
| admin_users | 管理员 |
| admin_roles | 角色 |
| admin_permissions | 权限 |
| admin_role_permissions | 角色权限关联 |
| admin_logs | 管理操作日志 |

## 字符集

统一 `utf8mb4_unicode_ci`，支持 emoji 与特殊符号。

## 索引命名

- `idx_xxx`：普通索引
- `uk_xxx`：唯一索引

## 软删除

所有用户表统一用 `deleted_at DATETIME NULL`，**禁止** 用 `is_deleted`。