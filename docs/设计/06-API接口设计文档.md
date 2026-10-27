# 毛团子AI · API 接口设计文档

**版本**：V2.0 | **更新日期**：2026-08-18 | **关联**：设计-04 技术架构、设计-05 数据库

> 本文档定义毛团子AI 前后端交互的所有 API 接口。**接口字段必须与设计-05 数据库表的字段一一对应**。

---

## 一、接口设计原则

| 原则 | 说明 |
|------|------|
| RESTful 风格 | 资源用复数名词，动作用 POST |
| 版本控制 | URL 路径 `/v1/`，重大变更发布 `/v2/` |
| 鉴权 | 除登录外需 `Authorization: Bearer <jwt>` |
| 幂等 | 写入操作支持 `Idempotency-Key` 请求头 |
| 时间 | ISO 8601 UTC：`2026-08-18T10:00:00Z` |
| 金额 | 整数分（cents），不传小数 |
| 分页 | `page` + `page_size`（默认 20，最大 100）|
| 限流 | 超限返回 429 + `Retry-After` |

---

## 二、通用响应结构

### 2.1 成功

```json
{
  "code": 0,
  "message": "ok",
  "data": { ... },
  "trace_id": "abc123"
}
```

### 2.2 失败

```json
{
  "code": 100101,
  "message": "宠物不存在",
  "data": null,
  "trace_id": "abc123"
}
```

### 2.3 分页

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "list": [...],
    "pagination": {
      "page": 1,
      "page_size": 20,
      "total": 100,
      "has_more": true
    }
  },
  "trace_id": "abc123"
}
```

---

## 三、鉴权机制

### 3.1 微信一键登录

**接口**：`POST /v1/auth/wx-login`

**请求**：

```json
{ "code": "wx_jscode_from_miniprogram" }
```

**响应**：

```json
{
  "code": 0,
  "data": {
    "token": "eyJhbGciOi...",
    "refresh_token": "rt_xxx",
    "expires_in": 604800,
    "user": {
      "id": 123,
      "openid": "oXyz...",
      "nickname": "毛团子妈",
      "avatar_url": "https://...",
      "vip_level": 0,
      "vip_expire_at": null,
      "is_new_user": true
    }
  }
}
```

**逻辑**：
1. 用 `code` 调微信 `/sns/jscode2session` 换 `openid`
2. 检查 `users` 表：有则登录，无则创建
3. 生成 JWT（access 7d + refresh 30d）

### 3.2 Token 刷新

**接口**：`POST /v1/auth/refresh`

```json
{ "refresh_token": "rt_xxx" }
```

### 3.3 登出

**接口**：`POST /v1/auth/logout`（鉴权）

将当前 JWT 加入黑名单（Redis TTL = 剩余有效期）。

### 3.4 JWT Payload

```json
{
  "user_id": 123,
  "openid": "oXyz...",
  "vip_level": 0,
  "iat": 1692345600,
  "exp": 1692950400
}
```

---

## 四、接口字段与数据库表对应

> **原则**：接口请求/响应字段名 = 数据库 snake_case 字段名（直接对应）。所有时间字段用 ISO 8601，所有金额用分。

### 4.1 时间字段序列化

| 数据库 | 接口 JSON |
|--------|----------|
| `created_at` (DATETIME) | `"2026-08-18T10:00:00Z"` |
| `next_run_at` (DATETIME) | `"2026-08-20T09:00:00Z"` |

### 4.2 金额字段

| 数据库 | 接口 JSON |
|--------|----------|
| `amount_cents INT` | `990`（不是 9.90） |

### 4.3 状态枚举

| 数据库 TINYINT | 接口 JSON | 含义 |
|---------------|----------|------|
| 0 | 0 | 通用初始态 |
| 1 | 1 | 通用成功态 |
| 2 | 2 | 通用失败态 |
| 3 | 3 | 已删除 |

各表具体状态定义见对应接口章节。

---

## 五、接口清单（按模块）

### 5.1 用户模块 `/v1`

| Method | 路径 | 鉴权 | 说明 |
|--------|------|:---:|------|
| POST | /auth/wx-login | 否 | 微信一键登录 |
| POST | /auth/refresh | 否 | 刷新 Token |
| POST | /auth/logout | 是 | 登出 |
| GET | /users/me | 是 | 当前用户信息 |
| PATCH | /users/me | 是 | 更新资料 |
| POST | /users/me/avatar | 是 | 上传头像（multipart） |
| POST | /users/me/cancel | 是 | 注销账号 |

#### `GET /v1/users/me`

**响应**：

```json
{
  "code": 0,
  "data": {
    "id": 123,
    "openid": "oXyz...",
    "nickname": "毛团子妈",
    "avatar_url": "https://...",
    "status": 0,
    "vip_level": 1,
    "vip_expire_at": "2026-09-18T10:00:00Z",
    "register_source": "direct",
    "last_login_at": "2026-08-18T10:00:00Z",
    "created_at": "2026-08-01T10:00:00Z"
  }
}
```

字段对应 `users` 表。

#### `PATCH /v1/users/me`

```json
{
  "nickname": "新昵称"
}
```

> **注意**：只能修改白名单字段（`nickname`、`avatar_url`）。敏感字段（`vip_level`、`status`）必须通过专门接口。

#### `POST /v1/users/me/cancel`

```json
{ "reason": "no_need" }
```

**逻辑**：
1. 校验账号状态（无未完成订单、未结算退款）
2. 标记 `status = 2`、`deleted_at = now()`
3. 15 天后定时任务硬删数据

---

### 5.2 宠物档案模块 `/v1/pets`

| Method | 路径 | 说明 |
|--------|------|------|
| GET | /pets | 列表 |
| POST | /pets | 创建 |
| GET | /pets/{id} | 详情 |
| PATCH | /pets/{id} | 更新 |
| DELETE | /pets/{id} | 删除 |

#### `GET /v1/pets`

**查询参数**：
- `species`（可选）：1=猫 2=犬
- `include_deleted`（可选，默认 false）

**响应**：

```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": 789,
        "user_id": 123,
        "nickname": "团子",
        "species": 1,
        "breed_id": 101,
        "breed_name": "英国短毛猫",
        "gender": 1,
        "birthday": "2024-05-01",
        "weight_kg": 4.5,
        "is_neutered": true,
        "allergies": {"items": [...], "notes": "..."},
        "staple_food": "皇家 K36",
        "avatar_url": "https://...",
        "remark": null,
        "created_at": "2026-08-01T10:00:00Z",
        "updated_at": "2026-08-18T10:00:00Z"
      }
    ],
    "pagination": { "page": 1, "page_size": 20, "total": 1, "has_more": false }
  }
}
```

#### `POST /v1/pets`

```json
{
  "nickname": "团子",
  "species": 1,
  "breed_id": 101,
  "gender": 1,
  "birthday": "2024-05-01",
  "weight_kg": 4.5,
  "is_neutered": true,
  "allergies": {"items": [{"type": "food", "name": "鸡胸肉"}]},
  "staple_food": "皇家 K36",
  "avatar_url": "https://..."
}
```

**校验**：
- 必填：`nickname`、`species`
- 免费用户最多 2 只（应用层）

---

### 5.3 AI 模块

##### `POST /v1/ai/tasks`

**请求**：

```json
{
  "type": "portrait_2d",
  "pet_id": 789,
  "style": "oil_painting",
  "image_url": "https://...",
  "is_preview": true
}
```

**字段说明**：

| 字段 | 必填 | 说明 |
|------|:---:|------|
| type | 是 | portrait_2d / portrait_3d_fast / portrait_3d_hd / emoji / card / copywriter / name / feeding_plan / symptom |
| pet_id | 否 | 关联宠物 |
| style | 视 type | 风格标识 |
| image_url | 视 type | 输入图 URL（已上传到 OSS） |
| is_preview | 否 | 是否仅预览（true=不扣额度、仅低清） |

**响应**：

```json
{
  "code": 0,
  "data": {
    "task_id": "t_abc123",
    "work_id": 456,
    "status": 0,
    "estimated_seconds": 30,
    "cost_quota": 1
  }
}
```

**逻辑**：
1. 校验类型、参数完整性
2. 校验额度（免费 / 会员）
3. 创建 `ai_tasks` 记录
4. 入队 BullMQ
5. 返回 `task_id`

##### `GET /v1/ai/tasks/{task_id}`

```json
{
  "code": 0,
  "data": {
    "task_id": "t_abc123",
    "work_id": 456,
    "status": 2,
    "progress": 100,
    "preview_url": "https://...",
    "result_url": "https://...",
    "extra_urls": {
      "hd_images": ["..."],
      "video_url": "..."
    },
    "error_code": null,
    "error_msg": null,
    "started_at": "2026-08-18T10:00:00Z",
    "finished_at": "2026-08-18T10:00:30Z"
  }
}
```

**状态枚举**（与 `ai_tasks.status` 一致）：

| 值 | 含义 | 是否终态 |
|:--:|------|:---:|
| 0 | pending 待处理 | 否 |
| 1 | processing 处理中 | 否 |
| 2 | success 成功 | ✅ |
| 3 | failed 失败（可重试）| 否 |
| 4 | cancelled 已取消 | ✅ |

##### `POST /v1/ai/tasks/{task_id}/cancel`

仅 `pending` / `processing` 状态可取消。

##### `GET /v1/ai/styles?type=portrait_2d`

返回当前可用的风格列表（带会员标识）。

##### `GET /v1/ai/templates?type=card`

返回模板列表。

##### 3D 模型相关

```
GET  /v1/3d/works/{id}/model       # 返回 GLB 签名 URL
POST /v1/3d/works/{id}/render      # 触发 4K 渲染图
POST /v1/3d/works/{id}/export      # 导出视频 / STL
```

---

### 5.4 作品模块 `/v1/works`

| Method | 路径 | 说明 |
|--------|------|------|
| GET | /works | 列表 |
| GET | /works/{id} | 详情 |
| DELETE | /works/{id} | 删除 |
| POST | /works/{id}/share | 记录分享 |

#### `GET /v1/works`

**查询参数**：
- `type`（可选）：过滤类型
- `is_public`（可选）
- `status`（可选）：默认 `status=2`（成功）

**响应**：

```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": 456,
        "user_id": 123,
        "pet_id": 789,
        "type": 1,
        "style": "oil_painting",
        "status": 1,
        "preview_url": "https://...",
        "result_url": "https://...",  // null 表示未付费
        "extra_urls": null,
        "is_public": false,
        "like_count": 0,
        "created_at": "2026-08-18T10:00:00Z"
      }
    ],
    "pagination": {...}
  }
}
```

**关键逻辑**：
- `result_url` 仅在用户已付费或会员时返回（**否则返回 null**）
- 服务端必须二次鉴权，前端不能依赖此字段

---

### 5.5 上传 `/v1/upload`

#### `POST /v1/upload/presign`（推荐）

**预签名直传 OSS，避免大文件过网关**。

**请求**：

```json
{
  "scene": "portrait_input",
  "content_type": "image/jpeg",
  "size": 1024000
}
```

**响应**：

```json
{
  "code": 0,
  "data": {
    "upload_url": "https://oss-cn-hangzhou.aliyuncs.com/...?Signature=...",
    "object_key": "uploads/2026/08/18/123/abc.jpg",
    "final_url": "https://cdn.maotuanzi.ai/uploads/2026/08/18/123/abc.jpg",
    "expires_at": "2026-08-19T10:00:00Z"
  }
}
```

#### `POST /v1/upload/image`（multipart，仅小文件）

直接走后端转发。

---

### 5.6 订单与会员 `/v1`

#### `GET /v1/products`

```json
{
  "code": 0,
  "data": {
    "products": [
      { "id": "portrait_single", "name": "高清宠物画像", "price_cents": 990, "type": "single" },
      { "id": "feeding_single", "name": "专属喂养方案", "price_cents": 1990, "type": "single" },
      { "id": "vip_month", "name": "月卡", "price_cents": 1990, "type": "subscription", "duration_days": 30 },
      ...
    ]
  }
}
```

> **价格由后端控制**，前端不展示硬编码价格。

#### `POST /v1/orders`

```json
{
  "product_id": "portrait_single",
  "coupon_id": null,
  "idempotency_key": "uuid"
}
```

**响应**：

```json
{
  "code": 0,
  "data": {
    "order_id": 1001,
    "order_no": "MT202608180001",
    "amount_cents": 990,
    "pay_amount_cents": 990,
    "wx_pay_params": {
      "timeStamp": "...",
      "nonceStr": "...",
      "package": "prepay_id=wx...",
      "signType": "RSA",
      "paySign": "..."
    },
    "expire_at": "2026-08-18T11:00:00Z"
  }
}
```

**逻辑**：
1. 校验幂等键（24h 复用）
2. 校验商品、优惠券
3. 计算价格
4. 创建订单（`status=0`）
5. 调微信支付统一下单
6. 返回 `prepay_id`

#### `POST /v1/webhooks/wx-pay`（微信回调）

微信支付 v3 异步通知，使用平台证书验签。

#### `POST /v1/orders/{id}/cancel`

仅 `status=0` 可取消。

#### `POST /v1/orders/{id}/refund`

```json
{ "reason": "效果不满意" }
```

**退款规则**（应用层校验）：
- 2D 画像 / 3D 手办：生成后 24 小时内、未使用、未分享 → 可全额退款；每用户每月限 1 次
- 喂养方案：未生成可退；已生成不支持退款（支持 ≤ 3 次调整）
- 会员、套餐、CPS：不支持退款

#### `GET /v1/vip/me`

```json
{
  "code": 0,
  "data": {
    "is_vip": true,
    "level": 1,
    "level_name": "月卡",
    "expire_at": "2026-09-18T10:00:00Z",
    "remaining_days": 30,
    "quota": {
      "portrait_per_month": 5,
      "portrait_used": 1,
      "3d_per_month": 0,
      "emoji_per_month": 3,
      "feeding_unlimited": true
  }
}
```

#### `GET /v1/vip/plans`

返回所有会员套餐及价格。

#### `GET /v1/coupons`

返回当前用户的可用券。

---

### 5.7 工具模块 `/v1/tools`

| Method | 路径 | 说明 |
|--------|------|------|
| GET | /tools/health-reminders | 健康提醒列表 |
| POST | /tools/health-reminders | 创建 |
| PATCH | /tools/health-reminders/{id} | 更新 |
| DELETE | /tools/health-reminders/{id} | 删除 |
| GET | /tools/weight-logs | 体重记录 |
| POST | /tools/weight-logs | 添加 |
| DELETE | /tools/weight-logs/{id} | 删除 |
| GET | /tools/journal | 时光手册 |
| POST | /tools/journal | 添加 |
| PATCH | /tools/journal/{id} | 更新 |
| DELETE | /tools/journal/{id} | 删除 |
| POST | /tools/symptom | 症状查询 |
| POST | /tools/feeding-plan | 生成喂养方案 |
| POST | /tools/name | AI 取名 |
| POST | /tools/copywriter | 文案生成 |

#### `POST /v1/tools/health-reminders`

```json
{
  "pet_id": 789,
  "type": 1,
  "title": "妙三多疫苗",
  "cycle_days": 365,
  "next_run_at": "2026-09-15T09:00:00Z",
  "note": "第一年三针已完成"
}
```

**校验**：
- 必填：`pet_id`、`type`、`title`、`next_run_at`
- 免费用户最多 3 条

#### `POST /v1/tools/symptom`

```json
{
  "symptom": "soft_stool",
  "pet_id": 789,
  "extra": "已持续 2 天"
}
```

**响应**：

```json
{
  "code": 0,
  "data": {
    "summary": "软便的可能原因与护理建议",
    "common_causes": ["饮食变化", "应激", "轻微肠胃不适"],
    "home_care": ["少食多餐", "观察粪便", "保证饮水"],
    "warning_signs": ["便血", "精神萎靡", "持续呕吐"],
    "disclaimer": "本内容仅供参考，不构成兽医诊断...",
    "related_product": {
      "id": "feeding_single",
      "name": "专属喂养方案",
      "price_cents": 1990
    }
  }
}
```

#### `POST /v1/tools/feeding-plan`

```json
{
  "pet_id": 789,
  "details": {
    "staple_food": "皇家 K36",
    "allergies": [...],
    "activity_level": "medium",
    "body_condition": "ideal"
  }
}
```

**响应**（与 `feedings.plan_content` JSON 对应）：

```json
{
  "code": 0,
  "data": {
    "feeding_id": 555,
    "plan_content": {
      "summary": "...",
      "daily_food": {...},
      "nutrition": {...},
      "schedule": [...],
      "warnings": [...]
    },
    "adjust_remaining": 3,
    "created_at": "2026-08-18T10:00:00Z"
  }
}
```

---

### 5.8 社交模块 `/v1/square`

| Method | 路径 | 说明 |
|--------|------|------|
| GET | /square/posts | 帖子流 |
| GET | /square/posts/{id} | 帖子详情 |
| POST | /square/posts | 发帖 |
| DELETE | /square/posts/{id} | 删帖 |
| POST | /square/posts/{id}/like | 点赞 |
| DELETE | /square/posts/{id}/like | 取消点赞 |
| GET | /square/users/{id} | 用户主页 |
| GET | /square/rank | 今日榜单 |
| GET | /square/match | 每日有缘 |
| POST | /square/match/{id}/greet | 打招呼 |
| GET | /square/challenges | 主题挑战 |
| POST | /square/challenges/{id}/checkin | 打卡 |

#### `GET /v1/square/posts`

**查询参数**：
- `tab`：latest / hottest / 3d_zone
- `species`（可选）：过滤品种
- `topic`（可选）：话题

**响应**：

```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": 8001,
        "user_id": 456,
        "user": { "id": 456, "nickname": "团子妈", "avatar_url": "..." },
        "work_id": 1234,
        "content": "我家团子的油画风画像，超级好看！",
        "image_urls": ["url1", "url2"],
        "topic": "portrait",
        "is_3d": false,
        "like_count": 23,
        "is_liked": false,
        "created_at": "2026-08-18T09:00:00Z"
      }
    ],
    "pagination": {...}
  }
}
```

**注意**：`is_liked` 是动态计算的（基于当前用户）。

---

### 5.9 通知与系统

| Method | 路径 | 说明 |
|--------|------|------|
| GET | /notifications | 列表 |
| PATCH | /notifications/{id}/read | 标记已读 |
| GET | /notifications/unread-count | 未读数 |
| GET | /system/banners | 首页 Banner |
| GET | /system/config | 全局配置（风格、模板、活动） |
| POST | /system/feedback | 意见反馈 |
| GET | /system/agreements/{type} | 获取协议 |

---

## 六、错误码规范

### 6.1 编码规则

格式：`ABCDEFG`（6 位数字）
- A：模块（1=通用 2=用户 3=宠物 4=AI 5=订单 6=会员 7=工具 8=社交 9=系统）
- B：类别（0=成功 1=参数 2=鉴权 3=权限 4=资源 5=状态 6=三方 3=限流 9=系统）
- CDE：具体编号

### 6.2 错误码表

| Code | HTTP | 含义 | 处理建议 |
|------|:---:|------|---------|
| 0 | 200 | 成功 | - |
| **通用** | | | |
| 100001 | 500 | 系统异常 | 提示用户重试 |
| 100002 | 503 | 服务维护 | 等待 |
| 100003 | 504 | 网关超时 | 重试 |
| 101001 | 400 | 参数错误 | 检查入参 |
| 101002 | 400 | 缺少必填参数 | 补全 |
| 101003 | 400 | 参数格式错误 | 检查类型 |
| 101004 | 400 | 文件过大 | 压缩 |
| 101005 | 415 | 文件类型不支持 | 换格式 |
| **鉴权** | | | |
| 102001 | 401 | 未登录 | 跳登录 |
| 102002 | 401 | Token 无效 | 跳登录 |
| 102003 | 401 | Token 过期 | 自动 refresh |
| 102004 | 401 | Refresh Token 失效 | 重新登录 |
| 103001 | 403 | 无权限 | 提示 |
| 103002 | 403 | 账号已封禁 | 提示 |
| 103003 | 403 | 账号已注销 | 重新注册 |
| **资源** | | | |
| 104001 | 404 | 资源不存在 | - |
| 104002 | 404 | 宠物不存在 | - |
| 104003 | 404 | 作品不存在 | - |
| 104004 | 404 | 订单不存在 | - |
| 104005 | 404 | 帖子不存在 | - |
| **状态** | | | |
| 105001 | 409 | 状态冲突 | 提示 |
| 105002 | 409 | 宠物数量超限（免费） | 升级会员 |
| 105003 | 409 | 提醒数量超限 | 升级会员 |
| 105004 | 409 | 额度不足 | 引导付费 |
| 105005 | 409 | 已退款，不可重复 | - |
| 105006 | 409 | 订单已支付 | - |
| 105007 | 409 | 任务状态不可操作 | - |
| **三方** | | | |
| 106001 | 502 | AI 服务商异常 | 重试 |
| 106002 | 504 | AI 生成超时 | 重试 |
| 106003 | 502 | 微信支付异常 | 提示 |
| 106004 | 502 | 微信登录异常 | 重试 |
| 106005 | 502 | OSS 上传失败 | 重试 |
| **限流** | | | |
| 109001 | 429 | 全局限流 | 等待 |
| 109002 | 429 | 用户限流 | 等待 |
| 109003 | 429 | IP 限流 | 等待 |

> **预留扩展**：每模块保留 50 个错误码（000-049）。

---

## 七、签名与幂等

### 7.1 请求签名（可选，敏感接口）

```
Headers:
  X-Timestamp: 1692345600
  X-Nonce: abc123def
  X-Sign: HMAC-SHA256(secret, method + path + body + timestamp + nonce)
```

**校验**：
- timestamp 在 5 分钟内有效
- nonce 一次性（Redis 去重）

### 7.2 幂等键

```
Headers:
  Idempotency-Key: <client-uuid>
```

24 小时内相同 key 仅执行一次，结果复用。

**适用场景**：创建订单、支付回调、用户操作。

---

## 八、限流策略

### 8.1 维度与阈值

| 维度 | 默认阈值 | 可配置 |
|------|---------|:---:|
| 全局 IP | 待实测 | ✅ |
| 单用户 | 待实测 | ✅ |
| 登录 | 10 次/小时/IP | ✅ |
| 创建订单 | 待实测 | ✅ |
| AI 任务创建 | 并发 2、总额按额度 | ✅ |
| 文件上传 | 待实测 | ✅ |
| 退款申请 | 3 次/30 天/用户 | ❌ |

### 8.2 实现

- 使用 Redis + 滑动窗口 / 令牌桶
- 限流中间件在 Caddy / NestJS 两层实施
- 超限返回 `429` + `Retry-After: <seconds>`

### 8.3 监控

- 触发限流时上报埋点
- 定期分析限流日志，调整阈值

---

## 九、版本控制

### 9.1 规则

- URL 路径：`/v1/`
- 兼容性原则：
  - ✅ 新增字段（可选）
  - ✅ 新增接口
  - ❌ 删除字段
  - ❌ 改变字段含义
  - ❌ 改变错误码

### 9.2 升级流程

```
发布 /v2/ → 同时保留 /v1/ 至少 6 个月 → 客户端迁移 → 下线 /v1/
```

---

## 十、API 文档生成

- **Swagger / OpenAPI 3.0**：NestJS Swagger 装饰器自动生成
- **客户端 SDK**：openapi-generator 自动生成 TypeScript SDK
- **Mock 服务**：Prism / 自研 mock server
- **文档托管**：部署到 `/docs` 路径或独立域名

---

## 十一、安全清单

- [x] HTTPS 全站
- [x] JWT + Refresh Token
- [x] 敏感接口签名
- [x] 限流（IP + 用户双维度）
- [x] SQL 注入防护（ORM 参数化）
- [x] XSS 防护（输出转义）
- [x] CSRF 防护（Token + SameSite）
- [x] 文件类型白名单
- [x] 文件大小限制
- [x] 内容审核
- [x] 操作日志
- [x] 异常告警

---

## 十二、未决问题（待 P0 阶段解决）

| # | 问题 | 影响 | 优先级 |
|---|------|------|:---:|
| 1 | 各接口具体限流阈值 | 需实测 | 🟠 P1 |
| 2 | 上传文件大小上限 | 需实测 3D 模型大小 | 🟠 P1 |
| 3 | AI 服务商响应时长 | 影响客户端超时设置 | 🟠 P1 |
| 4 | 是否需要图形验证码 | 防刷策略 | 🟡 P2 |

---

## 附录：相关文档

- 设计-04 技术架构设计文档
- 设计-05 数据库设计文档
- 开发-13 开发规范文档
- 开发-15 数据埋点与指标体系文档

---

> 📐 本文档是前后端联调、SDK 生成、Mock 服务的**唯一依据**。接口变更必须先更新本文档并通知前端。