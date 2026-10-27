# 毛团子AI · 后端接口审计 + 分阶段实施计划

> 架构师：高见远（software-architect-1）
> 范围：三项商业化决策落地审计 —— (a) 真实支付+会员/VIP 对齐；(b) 3D 真实渲染；(c) 分享奖励（邀请/裂变）；(d) 首页广场接真实后端 feed
> 约束：本报告仅做调研与方案，**不修改任何代码文件**。

代码根：
- 后端：`backend/src/main/java/com/maotuanzi`
- API 规范：`openapi/openapi.yaml`
- 移动端：`apps/mobile/entry/src/main/ets`

---

## A. 后端接口审计

判定标准：**存在 / 部分（数据或桩，不可直接商用）/ 缺失**。

| 能力 | 判定 | 关键文件 · 类 · 方法 |
|---|---|---|
| 支付/下单 createOrder | **部分** | `controller/api/OrderController.java#createOrder` POST `/v1/orders` (L28-34)；`service/impl/OrderServiceImpl.java#createOrder` (L39-70) 仅建 PENDING 单、写 `Order.orderNo/amountCents…`，**不调任何支付渠道、不返回 prepay 参数**，`Order.wxPrepayId` 永不赋值 |
| 统一下单 unified-order | **缺失** | 无任何 Controller/Service 返回微信 prepay_id 或华为 IAP 参数；`wechat.pay:0.4.8` 已在 `pom.xml` 声明但**全库零引用**（grep `com.wechat.pay/JsapiService/unifiedOrder/prepay` 无结果）；华为 IAP 全库零引用（grep `huawei/IapApi/HuaweiIap` 无结果） |
| 支付回调 | **部分（桩）** | `controller/webhook/WxPayWebhookController.java#wxPayCallback` POST `/v1/webhooks/wx-pay` (L25-37)：仅取 `out_trade_no/transaction_id`，注释明言"真实场景：验签+解密"——**当前无验签/解密**；调用 `OrderServiceImpl#markPaid` (L96-117) 幂等 OK、更新 PAID 并 `grantEntitlement`。`#wxRefundCallback` (L39-43) 纯桩 |
| 会员/VIP/订阅 | **部分** | 数据模型完整：`entity/User.java` `vipLevel/vipExpireAt` (L26-28)；JWT 携带 vip（`security/jwt/JwtUtil.java#L44-48`、`interceptor/AuthInterceptor.java#L63-64`、`security/UserContext` VIP_LEVEL）；开通 `OrderServiceImpl#grantEntitlement` (L119-155) 对 `productType==2` 开通，`VipLevelEnum.of` 映射 productId 字符串 `"vip_month"/"vip_quarter"/"vip_year"→1/2/3`，写 `VipRecord`+`userMapper.updateVip`；`common/enums/ProductTypeEnum` SINGLE=1/SUBSCRIPTION=2/PACKAGE=3；历史 `entity/VipRecord.java`+`VipRecordMapper`。**缺失**：对外查询 `/vip/me`、`/vip/plans` 在 openapi 已定义但**无对应 Controller/Service** |
| 用户权益（卡券/额度） | **部分** | 卡券数据层齐：`entity/Coupon.java`、`CouponClaim.java` + Mapper，但**无 claim/use API**（无 `CouponController`），`OrderServiceImpl#createOrder` 注释"卡券逻辑先空" (L51)；VIP 限额校验 `UserContext.getVipLevel()` 被 `PetServiceImpl#L59`、`ToolServiceImpl#L58` 使用（免费额度 exists） |
| 分享/邀请/奖励 | **缺失** | `SquarePost.shareCount` 字段存在 (`SquarePost.java#L24`) 但无 +1 API、无奖励、无邀请码、无裂变；无 `ShareController/RewardController/InviteController`；`User` 无 `invite_code` 字段 |
| 帖子/feed/广场 | **存在（完整 CRUD）** | `controller/api/SquareController.java`：`listPosts` GET `/v1/square/posts` (L29-35)、`detail`、`publish`、`delete`、`like/unlike`、`report`、`comments`+`addComment` 全齐；`SquarePost` 含 `is3d`(L20)、`shareCount`、`likeCount`、`commentCount`、`imageUrls`、`topic` |
| 3D 模型生成 | **缺失** | 无 3D 生成 Controller/Service/glTF 存储；`SquarePost.is3d` 存在但无 `model_url/gltf_url` 字段承载 3D 资产；`AiTaskCreate.type` 含 `portrait_3d_fast/portrait_3d_hd`（`openapi.yaml#L545`），`AiController` 链路存在但产出为图片 `result_url`，非 glTF，无 OSS glTF 接线 |

### A.2 OpenAPI 规范 vs 实现 不一致清单
- `/vip/me`、`/vip/plans`：规范已定义（`openapi.yaml#L317-329`），**未实现**
- `/orders/{id}/refund`：规范已定义（L299-315），**未实现**（仅 `admin/AdminOrderController` 退款审核，用户侧不可自助）
- `/orders` POST 返回：`OrderCreated.wx_pay_params`（`openapi.yaml#L609`）—— 实际返回 `Order` 实体，无该字段 → 规范与实现脱节
- `/square/posts` GET：规范 `tab` 枚举 `[latest,hottest,3d_zone]`（L384），实现用 `topic`+`status` 参数 → **tab 语义未实现**
- `/webhooks/wx-pay`：已实现（桩）
- **未文档化且需新增**：unified-order、华为 IAP、分享/奖励、3D 生成、邀请码

---

## B. 前端审计

### B.1 ApiService.ets 已暴露方法（按能力）
- **支付/订单**：`createOrder({productId,couponId?})`→POST `/v1/orders`、`getOrder(id)`、`listOrders(status,page,pageSize)`、`closeOrder(id)`
- **会员展示数据**：`me()`→`/v1/users/me` 返回 `vip_level`、`vip_expire_at`（`dto/LoginResponse.java#L34,L36`）
- **广场**：`listPosts(topic?,page,pageSize)`、`getPost`、`publishPost`、`deletePost`、`likePost`、`unlikePost`、`reportPost`、`getComments`、`addComment`
- **缺失方法**：`unifiedOrder`、`huaweiIapPay`、`wechatPay`、`vipMe`、`vipPlans`、`shareRecord/invite`、`model3dGenerate`、`couponClaim`

### B.2 页面占位 / UI 现状
- `pages/me/Index.ets`：VIP Banner → `pages/me/Vip`；显示 `user.vipLevel`（来自 `/users/me`）→ **会员展示已接后端字段**；"卡券中心"→`pages/me/Coupons`（已存在）
- `pages/me/Vip.ets`：会员中心完整 UI。`loadProducts()` 调 `ApiService.listProducts(2)` 过滤 `id` 以 `"vip_"` 开头，失败降级 `DEFAULT_PRODUCTS`（硬编码月/季/年）。`onPurchase()` 仅 `ApiService.createOrder` 后跳 `OrderDetail` → **无真实支付**；FAQ 写"支持微信、支付宝"均未接
- `pages/me/OrderDetail.ets`：`onPay()` 纯模拟（toast 模拟支付成功）；`status==1` 的"分享得免费券"按钮也是 toast 模拟 → **分享奖励未接**
- `pages/ai/Index.ets`：3D Tab = `comingSoonBlock("3D 萌宠数字手办")` + "去生成 3D"→`pages/ai/Upload`（通用上传，**无 3D 专属流程**）
- `pages/ai/Model3D.ets`：纯模拟（旋转渐变块），`isLocked` 写死 true，解锁弹窗"支付功能开发中"。**无 XComponent / glTF**
- `pages/square/Index.ets`：feed 已接 `ApiService.listPosts`（A 类已完成）；分享按钮 → toast "分享功能即将上线"（未接奖励）；Tab `latest/hot/3d/challenge` 直接作 `topic` 传入（与后端不匹配）
- `pages/square/PostDetail.ets`：已用 `QrCodeGenerator` 进页生成可扫二维码（A 类已完成）
- `utils/ShareSheet.ets`：系统分享面板（华为分享 Want）+ ActionMenu（微信/朋友圈/复制链接）；`getPostShareUrl`→`maotuanzi://square/post/{id}` 深链。**无后端记录/奖励调用**
- `utils/QrCodeGenerator.ets`：手写 QR 编码器 `QrCodeEncoder`（byte/EC-L/V1-5、`matrixToSvgDataUrl`），已验证可扫码。**可复用于邀请码分享卡**

---

## C. 分阶段实施计划（精确文件路径 + 顺序 + 并行 + 凭证降级）

### 阶段 0 · 契约对齐（最先，零凭证依赖，可独立）
- 后端：新增 `controller/api/VipController.java`（`GET /v1/vip/me`、`GET /v1/vip/plans`，复用 `ProductService.listActive(2)` + `UserService.me`）；补 `service/VipService`
- openapi：将 `/vip/me`、`/vip/plans` 标记为已实现；新增 `/orders/unified-order`、`/share/record`、`/share/invite`、`/rewards/claim`、`/ai/3d/tasks`；修正 `/square/posts` 的 `tab` 参数
- 前端：`ApiService.ets` 增 `vipMe()`、`vipPlans()`；`pages/me/Vip.ets#loadProducts` 改读 `/vip/plans`（降低对 `DEFAULT_PRODUCTS` 硬编码依赖）

### 阶段 1 · 支付抽象 + 统一下单 + 回调加固（核心，依赖阶段 0 契约，关键路径最长）
后端：
- 新增支付策略抽象：`service/PaymentChannel.java`（接口 `createPrepay(Order,User)→PrepayResult`）、`service/impl/WechatPayChannel.java`（用 `wechat.pay:0.4.8` `JsapiService` 调 unifiedOrder，落 `Order.wxPrepayId`）、`service/impl/HuaweiIapChannel.java`（调华为 IAP 建单，返回 `productId`+签名）
- `OrderController.java` 新增 `POST /v1/orders/unified-order`（入参 `orderId`+`channel`）→ 返回 `{channel, wxPayParams|iapParams, expireAt}`；`Order` 实体加 `channel` 字段区分通道
- `WxPayWebhookController#wxPayCallback` 加固：微信证书验签 + AES 解密；`markPaid` 已做 `wxTransactionId` 幂等，华为 IAP 交易号需同样幂等
- 退款：补 `OrderController` `POST /v1/orders/{id}/refund`（接已有 `Refund` 数据层 + `AdminServiceImpl#reviewRefund` 流程），对齐 openapi
- 配置降级：新增 `config/PaymentConfig.java`，`@Value("${MAOTUANZI_PAY_*}")` 全部 `${ENV}` 占位；**缺失密钥时 `PaymentChannel` 返回 mock prepay**，保证编译/运行不依赖凭证
- 配置项（均需负责人提供凭证）：`maotuanzi.pay.wechat.{mchId,apiKey,notifyUrl,certPath}`、`maotuanzi.pay.huawei.{appId,merchantId,key,productId}`

前端：
- 新建 `utils/PaymentManager.ets`：`interface PaymentStrategy { pay(order): Promise<PayResult> }`；`HuaweiIapPay`（调华为鸿蒙版 IAP SDK）、`WeChatPay`（微信鸿蒙版 SDK 拉起）、`MockStrategy`（无凭证时 toast+模拟回调）；统一 `pay(order, channel)`，通道不可用则购买按钮置灰 + "支付通道配置中"
- 改 `pages/me/Vip.ets#onPurchase` → 调 `PaymentManager.pay(unifiedOrder 结果)`
- 改 `pages/me/OrderDetail.ets#onPay` → 调 `PaymentManager`（替换模拟）

### 阶段 2 · 会员对齐（依赖阶段 0，与阶段 1 后端可并行）
后端：`VipController#/vip/me` 返回当前权益（读 `user.vipLevel/expireAt` + 剩余额度）；`/vip/plans` 返回套餐；补 `CouponController`（`POST /v1/coupons/claim` → 接 `CouponClaimMapper`，`OrderServiceImpl` 卡券逻辑填空）
前端：`pages/me/Index.ets` VIP Banner 已接 `vipLevel`，可显示到期日；`pages/me/Vip.ets` 套餐改读 `/vip/plans`、权益对比表改后端下发；`pages/me/Coupons.ets` 接 `/v1/coupons`

### 阶段 3 · 3D 真实渲染（依赖 AI 管线；后端独立，与阶段 1 并行）
后端：
- 新增 `controller/api/Model3DController.java`（`POST /v1/ai/3d/tasks`、`GET /v1/ai/3d/tasks/{id}`）、`service/Model3DService.java`、`entity/Model3DTask.java`（status, modelUrl, ossKey）；输入宠物图/参数 → 3D 重建 → 产出 glTF → `OssService.uploadWorkFile` 存 OSS → 返回 `model_url`（CDN）；`SquarePost` 增 `model_url` 字段承载 3D 资产，`is3d` 标记
- OSS 已具备（`OssServiceImpl` 存在，`enabled=${OSS_ENABLED:false}` 可降级）
前端：
- `pages/ai/Upload.ets` + `StyleSelect`：`portrait_3d_fast/hd` 走 3D 任务提交
- 新建 `components/Model3DViewer.ets`：XComponent 加载 glTF。**ArkTS 3D 选型**（不接第三方 3D 厂商云）：① 三方开源 glTF 解析+自绘；② XComponent + 原生 OpenGL/自绘；③ 华为图形能力。报告建议先用 ① 或 ② 本地渲染
- 改 `pages/ai/Model3D.ets`：用真实 `model_url` 替换模拟块；"生成 3D" 触发 3D 任务；VIP 解锁接真实订单
- `pages/ai/Index.ets` "去生成 3D" 改路由到 3D 专属生成流程（而非通用 Upload）

### 阶段 4 · 分享奖励（邀请/裂变）（独立，与阶段 2 后端可并行）
后端：
- `User` 增 `invite_code`（生成+唯一）；新增 `ShareController` + `RewardController` + `RewardService`：`POST /v1/share/record`（记分享，`SquarePost.shareCount+1`）、`POST /v1/share/invite`（绑 `invite_code`，注册/首单发奖）、`POST /v1/rewards/claim`（领奖写 `CouponClaim`/加额度）、`GET /v1/user/invite`（我的邀请码+进度）；`RewardRule` 配置
- 文件：`controller/api/ShareController.java`、`RewardController.java`、`service/RewardService.java`、`entity/InviteRelation.java`、`RewardRecord.java`
前端：
- 改 `utils/ShareSheet.ets`：分享成功后调 `ApiService.shareRecord(postId)` + 返奖 toast
- 新增 `pages/me/Invite.ets`（邀请码+进度）或并入 `me/Index`；**复用 `QrCodeGenerator.ets`** 生成"我的邀请码"分享卡（深链 `maotuanzi://invite/{code}`）
- `pages/square/Index.ets` 分享按钮接 `ShareSheet`+奖励；`OrderDetail.ets` "分享得免费券" 接真实分享/邀请奖励

### 阶段 5 · 广场 feed 收口（最后，依赖阶段 3/4 字段）
后端：`SquareController#list` 支持 `tab`（`latest`=时间、`hot`=likeCount、`3d_zone`=is3d=1、`challenge`=topic）映射
前端：`square/Index.ets` 修正 tab→参数映射；3D 专区卡片点击进 `Model3D` 预览
收口清单：
1. tab 语义对齐（前端传参 + 后端支持）
2. 分享按钮接奖励（阶段 4）
3. 3D 专区卡片接 `Model3D` 预览（阶段 3）
4. 字段映射：`post.imageUrl/previewUrl/styleTag/likeCount/commentCount` 与 `SquarePost` 对齐（**`SquarePost` 无 `styleTag` 字段** → 需补充或前端容错）

---

## D. 风险

1. **凭证/控制台依赖**：华为 IAP 需华为控制台 IAP 商品+商户配置；微信支付需商户号+APIv3 证书+回调域名备案。必须设计为可降级（mock），避免阻塞构建/运行。所有密钥走环境变量，缺失即 mock。
2. **ArkTS 3D 引擎选型**：鸿蒙原生 3D 能力有限；glTF 解析+渲染需自研或引入三方开源库，需评估包体与 API 12 兼容性；XComponent 原生渲染学习成本高、周期长。
3. **真实支付沙箱**：微信支付 sandbox 与华为 IAP 测试环境限制；端到端联调需真机+真实商户；**双通道对账**（`markPaid` 对微信 `wxTransactionId` 已幂等，华为 IAP 交易号需同机制）防重复入账。
4. **双支付退款**：`Order` 需 `channel` 字段区分；退款双向（华为/微信）各自回调处理，需分别幂等。
5. **规范与实现长期不一致**：本次须同步 openapi，否则前端联调踩坑（已发现 4 处脱节）。
6. **3D 资产存储与版权**：glTF 存 OSS 成本+CDN；建议 CDN 签名 URL 防盗刷。

---

## 实施顺序（分阶段 & 并行）

```
阶段0 契约对齐（先）
   └─ 阶段1 支付（核心/最长）──┬─ 阶段2 会员（并行）
                               ├─ 阶段3 3D（并行，后端独立）
                               └─ 阶段4 分享奖励（并行）
                                       └─ 阶段5 广场收口（最后，依赖3/4）
```
- 关键路径：`0 → 1 → (2,3,4) → 5`
- 后端可并行：0 / 2 / 3 / 4；前端各页改造随对应后端阶段推进
- 凭证降级点：阶段 1（支付 mock）、阶段 3（OSS 可关）、其余阶段零凭证依赖
