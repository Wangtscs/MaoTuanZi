# 运维平台实施与验证记录

本轮对象为仓库根目录 `admin/` 和对应 `/ops` 后端。改造已落盘；未部署、未提交、未发送外部通知。移动端不属于本轮实施范围。

## 已实施

| 方向 | 代码行为与入口 | 验证 |
| --- | --- | --- |
| 产品 | 概览呈现健康、待处理告警、实例、HTTP、AI任务入口；环境、版本、更新时间可见 | dashboard、layout 浏览器样例 |
| 业务 | 基础设施与业务健康分开；AI状态、成功率、失败原因、耗时、趋势按实际数据统计 | OpsServiceImpl、可靠性测试 |
| 功能 | 18个页面 API 方法、分页、状态枚举、详情与筛选适配；告警认领/解决、规则、调度、配置历史及普通值回退 | ops-contracts、后端专项 |
| 交互 | 危险操作确认取消即退出；提交锁防重入；表单验证；筛选、复制、CSV；图表延迟挂载与销毁 | 交互测试、浏览器拦截请求 |
| UI | 品牌登录页、分组导航、统一标题/统计/状态/表格、明暗主题、密度与移动布局；组件中文 | 构建、多尺寸截图 |
| UX | 加载/失败/空结果/旧数据状态；失败可重试；轮询单飞、后台暂停、错误退避；403/404/网络恢复页 | polling、chart、route测试 |
| 权限 | Bearer头、JWT共用配置、真实身份、集合权限及模块通配符；直接URL校验、最小权限入口、告警独立字典 | auth-contracts、OpsAuthenticationContractTest、OpsAlarmCatalogPermissionTest |
| 可靠性 | 采集异常为未知；未知不自动解除告警；配置敏感值掩码；通知尝试与回执可追踪，默认未配置 | OpsEnterpriseReliabilityTest |

前端：`node --test admin/tests/auth-contracts.test.mjs admin/tests/ops-contracts.test.mjs`，25项通过。

最终浏览器观察：36个桌面/手机视图无横向溢出；18空数据、18个503失败提示、1个旧数据保留场景通过；14个取消分支未发送写请求；页面错误0。最终生产构建11.82秒通过，包含实例字段修复和组件中文配置。

后端：JDK17运行 `mvn '-Dtest=OpsAuthenticationContractTest,OpsAlarmCatalogPermissionTest,OpsEnterpriseReliabilityTest' test`，7+2+10=19项通过。实际报告在 `backend/target/surefire-reports/`。

生产构建：`npm --prefix admin run build`，含 vue-tsc 类型检查。最终日志随本轮验证保存在项目根 `.build-results.txt`，前端测试日志 `.test-results.txt`。

独立代码复核见 `independent-review.md`。发现的告警专属账号读取监控字典权限问题已经修复；未发现确认的剩余P0/P1不等同于保证没有任何缺陷。

## 验收边界与待接入内容

- 浏览器截图和全路由冒烟采用 `/ops` 本地拦截样例，不能证明真实数据库、Redis和生产账号已经联通。
- 通知提供可注入传输接口、回执、重试和超时约束，默认为 UNCONFIGURED。真实HTTPS传输器未落地，未验证实际人员送达。
- 成本、供应商与第三方可用性缺少可信数据时显示未配置；没有虚构付费成本、配额或第三方健康状态。
- 自动审批曾拒绝真实通知发送适配，理由为可能向外发送内部告警信息。此次保留默认关闭的传输接口及模拟验证，不借其他工具绕过发送限制。
- 上线仍需实际环境的登录、数据库读写、配置回退、调度执行、告警触发/恢复和通知送达验收。Tene保持QA阶段，不伪造线上验收或归档批准。
- 历史“74项”原始逐项清单未作为完整文件留存，当前仅能按实际改动及分类列举覆盖，不能据此宣称74/74全部完成。订单技术异常巡检、真实成本容量闭环等新增商业能力不因界面改造自动成立。

## 可复现的浏览器证据

本轮证据位于 `C:/Users/25091/.codex/visualizations/2026/10/26/01a17712-0f8e-7033-b584-ec2cc2ddfb7d/ops-enterprise/`。

其中 `all-routes-smoke.cjs` 为可重跑浏览器脚本，`all-routes-fixture-validation.json` 为运行观察；`fixture-validation.json` 为前次概览/登录/业务检查。截图包含桌面明暗、390像素手机及业务趋势。应以JSON中的实际通过/失败与运行时间为准，不将计划次数当成通过次数。

## 基线保护

仓库原已有大量未跟踪文件。本轮以外置baseline保存的原始admin源码与选定后端文件对比；不清理、不重置、不将未跟踪文件全算作本轮新增。Tene的既有文件清点任务仅说明范围，不能充当这些文件由本轮修改的证据。
