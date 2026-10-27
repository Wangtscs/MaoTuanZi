# 毛团子AI · 全栈企业级商用项目

> 用 AI 让每一只毛孩子被温柔以待
> V2.0 · 2026-08-20 · 商用可上线版本

---

## 一、项目概述

毛团子AI 是一款面向宠物主的 AI 创作 + 养宠工具应用，覆盖 AI 画像、3D 手办、喂养方案、症状查询、健康提醒、轻社交等完整业务链路。

### 形态

| 端 | 技术栈 | 用途 |
|---|---|---|
| **鸿蒙移动端** | HarmonyOS + ArkTS + ArkUI | C 端用户主入口 |
| **管理后台** | Vue 3 + TypeScript + Element Plus | 运营管理 |
| **后端服务** | Java 17 + Spring Boot 2.7 + MyBatis | 业务 API |
| **数据库** | MySQL 8.0 + Redis 6 + 阿里云 OSS | 数据 / 缓存 / 文件 |
| **AI 网关** | Python FastAPI（mock 优先） | AI 任务编排（可扩展） |

---

## 二、目录结构

```
MaoTuanZi/
├── apps/
│   ├── mobile/                 # 鸿蒙 ArkTS 客户端
│   │   └── entry/
│   │       └── src/main/
│   │           ├── ets/        # ArkTS 源码（27 个页面 + 72 个 SVG 图标）
│   │           └── resources/  # 资源 / 颜色 / 文案
│   └── admin/                  # Vue3 管理后台
│       └── src/                # 11 个业务页面
├── backend/                    # Spring Boot 后端
│   └── src/main/
│       ├── java/com/maotuanzi/ # 152 个 Java 文件
│       │   ├── controller/      # REST 控制器
│       │   ├── service/         # 业务逻辑
│       │   ├── mapper/          # MyBatis 数据访问
│       │   ├── entity/          # 数据库实体
│       │   ├── dto/             # 请求/响应 DTO
│       │   ├── config/          # 配置类
│       │   ├── interceptor/     # 拦截器
│       │   ├── security/        # JWT 安全
│       │   ├── common/          # 通用工具
│       │   ├── annotation/      # 自定义注解
│       │   ├── task/            # 定时任务
│       │   └── listener/        # 监听器
│       └── resources/
│           ├── application.yml
│           └── mapper/          # 26 个 MyBatis XML
├── db/                          # 数据库脚本
│   ├── schema.sql              # 一键建库（30 张表）
│   └── seeds/                   # 种子数据（品种/商品/敏感词/管理员）
├── docs/                        # 文档
│   ├── 设计/                    # 设计文档
│   ├── 开发/                    # 开发文档
│   ├── 管理/                    # 管理文档
│   ├── 合规/                    # 合规文档
│   ├── 合规法律/                 # 法务文档
│   ├── 运营/                    # 运营文档
│   ├── 测试/                    # 测试文档
│   ├── 部署运维/                # 部署文档
│   └── deploy/                  # 部署 SOP + 检查清单
├── prd2.md                     # 产品需求文档
├── .env.example                # 环境变量模板
└── README.md                   # 本文件
```

---

## 三、技术栈

### 3.1 鸿蒙移动端（ArkTS）

- **语言**：ArkTS（TypeScript 超集）
- **UI**：ArkUI 声明式开发范式
- **API 版本**：5.0.0 (API 12)
- **图标系统**：72 个自研 SVG 矢量图标（**严禁使用 emoji**）
- **设计风格**：治愈系精致，毛团粉 (#FFB6C1) + 毛团橘 (#FFAA80) + 奶油白 (#FFF8F0)
- **页面数量**：27 个，覆盖首页、AI 创作、工具、广场、我的全部场景

### 3.2 Vue3 管理后台

- **框架**：Vue 3.4 + Composition API
- **语言**：TypeScript 5.3
- **UI 库**：Element Plus 2.4
- **状态管理**：Pinia
- **HTTP**：Axios
- **构建**：Vite 5

### 3.3 Spring Boot 后端

- **Java**：17（兼容 8）
- **框架**：Spring Boot 2.7.18
- **数据访问**：MyBatis + PageHelper
- **安全**：Spring Security Crypto（仅密码加密）+ JWT（jjwt 0.11.5）
- **API 文档**：Knife4j 4.4（Swagger）
- **工具**：Hutool 5.8、Fastjson2
- **数据库**：MySQL 8.0
- **缓存**：Redis 6 + Caffeine
- **监控**：Micrometer + Prometheus

### 3.4 数据库

- **MySQL 8.0**：30 张业务表
- **字符集**：utf8mb4_unicode_ci
- **软删除**：统一 `deleted_at` DATETIME NULL
- **金额**：整数分（cents）

---

## 四、核心特性

### 4.1 业务功能（按 MVP 范围）

- ✅ 用户体系（微信一键登录 / 注册）
- ✅ 宠物档案 CRUD
- ✅ AI 2D 画像（10 种风格）
- ✅ 喂养方案生成
- ✅ 症状查询（含免责声明）
- ✅ 健康提醒
- ✅ 微信支付（订单 / 会员 / 退款）
- ✅ 个人中心（作品 / 订单 / 会员 / 设置）
- ✅ 萌宠广场（帖子 / 点赞 / 评论 / 举报）
- ✅ 30 张表 + 60+ 品种字典
- ✅ 完整管理后台（用户 / 作品 / 订单 / 退款 / 内容 / 敏感词 / 统计）

### 4.2 企业级架构

- ✅ 统一响应结构（ApiResult）
- ✅ 全局异常拦截
- ✅ JWT 鉴权 + Refresh Token
- ✅ 接口限流（基于 Redis）
- ✅ 幂等键校验
- ✅ 操作日志（管理后台）
- ✅ 审计日志
- ✅ 定时任务（订单过期 / 健康提醒推送 / 幂等键清理）
- ✅ Prometheus 监控
- ✅ 跨域 CORS
- ✅ API 文档（Knife4j）
- ✅ 国际化（i18n）预留

### 4.3 安全合规

- ✅ BCrypt 密码加密
- ✅ JWT Token 黑名单
- ✅ 微信支付商户证书支持
- ✅ OSS 私有读 + 签名 URL
- ✅ 内容审核接口（机审 + 敏感词）
- ✅ 医疗免责声明（顶部固定展示）
- ✅ 数据导出 / 注销（GDPR）
- ✅ 软删除 + 30 天后硬删
- ✅ SQL 注入防护（MyBatis 参数化）

---

## 五、API 接口

### 5.1 接口规范

- 路径前缀：`/v1`
- 鉴权：JWT (`Authorization: Bearer <token>`)
- 时间格式：ISO 8601 UTC
- 金额：整数分
- 分页：`page` + `page_size`（默认 20，最大 100）
- 响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": { ... },
  "trace_id": "abc123"
}
```

### 5.2 接口模块

| 模块 | 路径 | 说明 |
|---|---|---|
| 认证 | `/v1/auth/*` | wx-login / refresh / logout |
| 用户 | `/v1/users/*` | me / cancel / export |
| 宠物 | `/v1/pets/*` | CRUD |
| 品种 | `/v1/breeds/*` | 字典 |
| 商品 | `/v1/products/*` | 商品列表 / 详情 |
| AI | `/v1/ai/*` | styles / tasks / works |
| 订单 | `/v1/orders/*` | create / list / close |
| 作品 | `/v1/works/*` | list / unlock / delete |
| 工具 | `/v1/tools/*` | reminders / weights / journal / feedings / feeding-plan / symptom |
| 广场 | `/v1/square/*` | posts / likes / comments / reports |
| 通知 | `/v1/notifications/*` | list / read / unread-count |
| 后台 | `/admin/*` | 用户/作品/订单/退款/内容/敏感词/统计 |

完整文档：`http://localhost:8080/doc.html`

---

## 六、快速开始

### 6.1 环境要求

| 项 | 版本 |
|---|---|
| JDK | 17（兼容 8，编译后端） |
| Maven | 3.9+ |
| Node | 20 LTS |
| MySQL | 8.0+ |
| Redis | 6+ |
| DevEco Studio | 4.0+ |

### 6.2 启动后端

```bash
cd backend
mvn clean compile -DskipTests
mvn spring-boot:run
# → http://localhost:8080
# → API 文档 http://localhost:8080/doc.html
```

### 6.3 初始化数据库

```bash
mysql -u root -p < db/schema.sql
mysql -u root -p < db/seeds/breeds.sql
mysql -u root -p < db/seeds/products.sql
mysql -u root -p < db/seeds/admin.sql
mysql -u root -p < db/seeds/sensitive.sql
```

### 6.4 启动管理后台

```bash
cd apps/admin
npm install
npm run dev
# → http://localhost:5173
# 默认账号：admin / admin123
```

### 6.5 启动鸿蒙端

```bash
# 用 DevEco Studio 打开 apps/mobile/
# 选择 Run → 运行到设备
```

---

## 七、核心设计亮点

### 7.1 视觉设计

- 治愈系精致风格，温暖有情感
- 72 个自研 SVG 矢量图标（线条 + 填充双形态）
- 5 个 Tab 配套高亮态
- 状态机驱动的 UI 反馈（加载 / 空 / 错误 / 成功）
- 医疗免责声明视觉醒目且不可关闭
- 动画过渡流畅（页面切换 / 弹窗 / 进度条）

### 7.2 交互闭环

- 每个操作有加载 / 成功 / 失败反馈
- 网络异常 / 权限不足 / AI 生成失败 / 支付异常都有友好兜底
- 一致的状态文案体系（参考 `docs/设计/品牌与文案规范.md`）

### 7.3 数据完整性

- 软删除统一规范
- 状态机驱动（AI 任务 / 订单 / 退款 / 会员 / 作品 / 帖子 / 提醒）
- 关键业务规则在文档中明确（参考 `docs/设计/状态机与业务规则汇总.md`）

---

## 八、文档索引

| 文档 | 用途 |
|---|---|
| `prd2.md` | 产品需求 |
| `docs/设计/03-信息架构与页面流程图.md` | IA + 页面 |
| `docs/设计/04-技术架构设计文档.md` | 技术架构 |
| `docs/设计/05-数据库设计文档.md` | 数据库 |
| `docs/设计/06-API接口设计文档.md` | API |
| `docs/设计/UIUX视觉规范与组件库.md` | 视觉规范 |
| `docs/设计/状态机与业务规则汇总.md` | 业务规则 |
| `docs/设计/品牌与文案规范.md` | 文案规范 |
| `docs/设计/接口错误码清单.md` | 错误码 |
| `docs/管理/MVP范围摘要.md` | MVP 范围 |
| `docs/合规/医疗免责声明使用规范.md` | 医疗合规 |
| `docs/合规/内容审核规范.md` | 内容审核 |
| `docs/合规/数据主体权利响应流程.md` | GDPR |
| `docs/合规法律/20-隐私政策.md` | 隐私政策 |
| `docs/合规法律/21-用户服务协议.md` | 服务协议 |
| `docs/部署运维/部署SOP.md` | 部署 SOP |
| `docs/部署运维/上线检查清单.md` | 上线清单 |
| `docs/develop/*` | 开发文档 |
| `docs/deploy/部署SOP.md` | 生产部署 |
| `docs/deploy/上线检查清单.md` | 上线检查 |

---

## 九、已知 TODO

- AI 网关（Python FastAPI）目前 mock，需后续集成
- 3D 手办模型渲染（占位 mock）
- 实时推送（WebSocket 占位）
- 内容审核（机审 API 待接入）
- 推送通知（订阅消息）

---

## 十、版本

- **v2.0.0** · 2026-08-20 · 全栈企业级商用版本
- v1.0.0 · 2026-08-15 · MVP 单后端
- v0.1.0 · 2026-08-10 · 文档驱动阶段

---

> ❤️ 毛团子AI · 让每一只毛孩子都被温柔以待