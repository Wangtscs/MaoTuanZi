# 毛团子 · 运维平台前端

Vue3 + Vite + TypeScript + Element Plus + Pinia + ECharts 实现的**技术运维/SRE 控制台**。支持明暗主题、紧凑密度、手机导航、角色菜单及直接访问鉴权。
后端接口统一前缀 `/ops/**`，与移动端 `/v1/**`、业务后台 `/admin/**` 完全隔离。

## 功能模块

| 菜单 | 路由 | 后端接口 | 权限码 |
| --- | --- | --- | --- |
| 运维概览 | `/dashboard` | `GET /ops/overview` | ops.view |
| 实时监控 | `/monitor/realtime` | `GET /ops/monitor/realtime`, `/timeline` | ops.metrics |
| 指标趋势 | `/monitor/metrics` | `GET /ops/monitor/catalog`, `/series`, `/metrics` | ops.metrics |
| 系统资源 | `/system/resource` | `GET /ops/system/resource` | ops.system |
| 线程快照 | `/system/threads` | `GET /ops/system/threads` | ops.system |
| 日志中心 | `/logs` | `GET /ops/logs`, `/stat`, `PUT /level`, `DELETE` | ops.log |
| 告警规则 | `/alarm/rules` | `GET/POST/PUT/DELETE /ops/alarm/rules`, `/toggle`, `/preview`, `/evaluate` | ops.alarm |
| 告警记录 | `/alarm/records` | `GET /ops/alarm/records`, `/ack`, `/resolve` | ops.alarm |
| 任务调度 | `/job/list` | `GET/POST/PUT/DELETE /ops/jobs`, `/toggle`, `/trigger`, `/handlers` | ops.job |
| 任务记录 | `/job/records` | `GET /ops/jobs/records` | ops.job |
| 数据源·数据库 | `/datasource/db` | `GET /ops/datasource/db`, `/db/tables` | ops.datasource |
| 数据源·Redis | `/datasource/redis` | `GET /ops/datasource/redis` | ops.datasource |
| 存储概览 | `/storage` | `GET /ops/storage/overview` | ops.storage |
| 业务巡检 | `/biz/inspect` | `GET /ops/biz/inspect` | ops.view |
| 管理审计 | `/audit/admin` | `GET /ops/audit/admin-logs` | ops.audit |
| 内容审计 | `/audit/content` | `GET /ops/audit/content-logs` | ops.audit |
| 运维配置 | `/config` | `GET/POST/DELETE /ops/configs` | ops.config |
| 实例列表 | `/instances` | `GET /ops/instances` | ops.view |

## 本地开发

```bash
# 依赖（首次）
npm install

# 开发服务器（默认 5173，/ops、/admin 代理到 http://localhost:8080）
npm run dev

# 生产构建（含类型检查）
npm run build
npm run preview
```

自定义后端地址：环境变量 `VITE_BACKEND_TARGET`（如 `http://192.168.1.10:8080`）。

## 登录与鉴权

- 复用业务后台的 Admin JWT：登录调用 `POST /ops/auth/login`（内部复用 `AdminService.login`），校验账号是否具备 `ops.*` 权限，否则拒绝。
- 登录后前端将 `X-Admin-Token: Bearer <token>` 放入请求头；所有受保护的 `/ops/**` 由 `AdminAuthInterceptor` + `@RequireAdmin` 校验。账号以实际数据库为准，本说明不承诺默认凭据。
- JWT 签发和解析共用后端配置。403保留会话并提示权限；401清理登录并返回登录页；身份查询断网进入可重试页面。仅告警权限可独立读取 `/ops/alarm/metric-catalog`，不会取得监控采样权限。

## 目录结构

```
admin/
├─ index.html
├─ vite.config.ts        # 代理 /ops、/admin → 后端
├─ tsconfig*.json
├─ package.json
└─ src/
   ├─ main.ts            # 入口（Element Plus 暗色 + Pinia + Router）
   ├─ App.vue
   ├─ styles/index.css   # 暗色主题变量
   ├─ api/request.ts     # axios 封装：X-Admin-Token、ApiResult 解包、401 跳转
   ├─ api/ops.ts         # 全部 /ops/** 接口
   ├─ router/index.ts    # 路由 + 权限元信息 + 登录守卫
   ├─ stores/            # user（登录态/权限）、app（折叠/主题）
   ├─ layout/index.vue   # 侧边栏 + 顶栏（按权限过滤菜单）
   ├─ components/        # StatCard 等复用组件
   ├─ utils/echarts.ts   # ECharts 暗色封装
   └─ views/             # 各功能页
```

## 说明

- API适配以实际控制器方法、字段、分页为准；公共页面状态区分加载、空结果、失败、保留的旧数据。采集失败显示未知，不能当作零或健康。
- 监控/日志/告警为**进程内**采集：HTTP 指标来自全局拦截器滑动窗口，日志来自 Logback Appender 注入的内存环形缓冲；无需外部 APM。
- 指标快照、告警记录、任务记录落库到 `ops_*` 表（见 `backend/.../db/migration/V2__ops.sql`）。

## 本地回归与上线边界

从仓库根目录运行：

```powershell
node --test admin/tests/auth-contracts.test.mjs admin/tests/ops-contracts.test.mjs
npm --prefix admin run build
cd backend
mvn '-Dtest=OpsAuthenticationContractTest,OpsAlarmCatalogPermissionTest,OpsEnterpriseReliabilityTest' test
```

配置支持历史与可回退的普通值；敏感值掩码且不保留可还原明文历史。通知包含通道接口、尝试次数及回执记录；默认传输器为 UNCONFIGURED，必须接入真实服务后另做送达验收。成本和供应商可用性缺少可信数据时显示未配置。

浏览器截图与多路由冒烟使用本地拦截样例，仅证明界面和交互。Maven专项用例使用受控依赖，不能替代实际MySQL、Redis、账号、数据、通知通道及部署环境验收。此次不部署、不改移动端、不发送外部通知。
