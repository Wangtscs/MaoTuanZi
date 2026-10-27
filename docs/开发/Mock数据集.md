# 毛团子AI · Mock 数据集

**版本**：V2.0 | **更新日期**：2026-08-18 | **关联文档**：`docs/开发/数据库迁移与种子数据.md`、`docs/设计/05-数据库设计文档.md`

> 本文档提供开发期前后端联调用的 Mock 数据。**所有数据字段与数据库表结构保持一致**。生产环境使用真实数据，测试环境使用 Factory 自动生成。

---

## 一、用户

### 1.1 普通用户

```json
{
  "id": 100001,
  "openid": "mock_openid_001",
  "nickname": "团子妈",
  "avatar_url": "https://cdn.maotuanzi.ai/mock/avatar-female-01.jpg",
  "status": 0,
  "vip_level": 0,
  "vip_expire_at": null,
  "created_at": "2026-08-01T08:00:00Z"
}
```

### 1.2 月卡会员

```json
{
  "id": 100002,
  "openid": "mock_openid_002",
  "nickname": "毛球爸",
  "avatar_url": "https://cdn.maotuanzi.ai/mock/avatar-male-01.jpg",
  "status": 0,
  "vip_level": 1,
  "vip_expire_at": "2026-09-18T10:00:00Z",
  "created_at": "2026-07-15T08:00:00Z"
}
```

### 1.3 已注销用户

```json
{
  "id": 100003,
  "openid": "mock_openid_003",
  "nickname": "已注销用户",
  "avatar_url": "",
  "status": 2,
  "vip_level": 0,
  "vip_expire_at": null,
  "created_at": "2026-06-01T08:00:00Z"
}
```

---

## 二、宠物档案（与 breeds 字典对齐）

### 2.1 英短猫

```json
{
  "id": 200001,
  "user_id": 100001,
  "nickname": "团子",
  "species": 1,
  "breed_id": 101,
  "breed_name": "英国短毛猫",
  "gender": 1,
  "birthday": "2022-05-01",
  "weight_kg": 4.5,
  "is_neutered": true,
  "allergies": {
    "items": [{ "type": "food", "name": "鸡胸肉", "severity": "mild" }],
    "notes": "易软便"
  },
  "staple_food": "皇家 K36",
  "avatar_url": "https://cdn.maotuanzi.ai/mock/pet-cat-01.jpg",
  "remark": "性格温顺",
  "created_at": "2026-08-01T08:00:00Z"
}
```

### 2.2 柯基犬

```json
{
  "id": 200002,
  "user_id": 100001,
  "nickname": "豆豆",
  "species": 2,
  "breed_id": 201,
  "breed_name": "柯基",
  "gender": 1,
  "birthday": "2025-03-15",
  "weight_kg": 8.2,
  "is_neutered": false,
  "allergies": null,
  "staple_food": "渴望幼犬粮",
  "avatar_url": "https://cdn.maotuanzi.ai/mock/pet-dog-01.jpg",
  "remark": "",
  "created_at": "2026-08-10T08:00:00Z"
}
```

### 2.3 田园猫（免费用户第三只，用于超限测试）

```json
{
  "id": 200003,
  "user_id": 100001,
  "nickname": "咪咪",
  "species": 1,
  "breed_id": 199,
  "breed_name": "中华田园猫",
  "gender": 2,
  "birthday": "2023-08-20",
  "weight_kg": 3.8,
  "is_neutered": true,
  "allergies": null,
  "staple_food": "网易严选猫粮",
  "avatar_url": "https://cdn.maotuanzi.ai/mock/pet-cat-02.jpg",
  "created_at": "2026-08-15T08:00:00Z"
}
```

---

## 三、品种字典（与 breeds 表字段对齐）

```json
[
  {
    "id": 101,
    "species": 1,
    "name": "英国短毛猫",
    "name_en": "British Shorthair",
    "aliases": "英短",
    "description": "体型圆胖、四肢粗短、毛短而密",
    "characteristics": "温顺、亲人、不爱叫",
    "care_tips": "控制饮食预防肥胖",
    "image_url": "https://cdn.maotuanzi.ai/mock/breed-cat-101.jpg",
    "sort_order": 10,
    "is_active": 1
  },
  {
    "id": 102,
    "species": 1,
    "name": "美国短毛猫",
    "name_en": "American Shorthair",
    "aliases": "美短",
    "description": "体型匀称、毛色多样",
    "characteristics": "活泼、聪明、易相处",
    "care_tips": "需要适当运动",
    "image_url": "https://cdn.maotuanzi.ai/mock/breed-cat-102.jpg",
    "sort_order": 20,
    "is_active": 1
  },
  {
    "id": 103,
    "species": 1,
    "name": "布偶猫",
    "name_en": "Ragdoll",
    "aliases": "仙女猫",
    "description": "体型大、毛发长、眼睛蓝",
    "characteristics": "温顺、粘人、容忍度高",
    "care_tips": "每日梳毛，注意毛发打结",
    "image_url": "https://cdn.maotuanzi.ai/mock/breed-cat-103.jpg",
    "sort_order": 30,
    "is_active": 1
  },
  {
    "id": 199,
    "species": 1,
    "name": "中华田园猫",
    "name_en": "Chinese Li Hua",
    "aliases": "土猫,狸花猫",
    "description": "本土自然品种，适应力强",
    "characteristics": "聪明、独立、身体素质好",
    "care_tips": "按普通家猫喂养即可",
    "image_url": "https://cdn.maotuanzi.ai/mock/breed-cat-199.jpg",
    "sort_order": 90,
    "is_active": 1
  },
  {
    "id": 201,
    "species": 2,
    "name": "柯基",
    "name_en": "Corgi",
    "aliases": "威尔士柯基",
    "description": "腿短臀圆、耳朵立",
    "characteristics": "活泼、聪明、警觉",
    "care_tips": "注意脊椎健康，避免频繁上下楼梯",
    "image_url": "https://cdn.maotuanzi.ai/mock/breed-dog-201.jpg",
    "sort_order": 10,
    "is_active": 1
  },
  {
    "id": 202,
    "species": 2,
    "name": "金毛寻回犬",
    "name_en": "Golden Retriever",
    "aliases": "金毛",
    "description": "中大型犬，毛发金黄",
    "characteristics": "友善、温顺、聪明",
    "care_tips": "需要大量运动，注意髋关节健康",
    "image_url": "https://cdn.maotuanzi.ai/mock/breed-dog-202.jpg",
    "sort_order": 20,
    "is_active": 1
  },
  {
    "id": 203,
    "species": 2,
    "name": "贵宾犬",
    "name_en": "Poodle",
    "aliases": "泰迪",
    "description": "卷毛、体型分大中小",
    "characteristics": "聪明、活泼、不掉毛",
    "care_tips": "定期美容，注意耳部清洁",
    "image_url": "https://cdn.maotuanzi.ai/mock/breed-dog-203.jpg",
    "sort_order": 30,
    "is_active": 1
  }
]
```

> 完整品种库需补充至 100+。格式按 [`docs/开发/数据库迁移与种子数据.md`](数据库迁移与种子数据.md) § 3.2。

---

## 四、AI 风格（与 styles 字段对齐）

```json
[
  {
    "id": "oil_painting",
    "type": "portrait_2d",
    "name": "油画风格",
    "cover_url": "https://cdn.maotuanzi.ai/mock/style-oil.jpg",
    "is_member_only": false,
    "price_cents": null,
    "sort_order": 10
  },
  {
    "id": "disney",
    "type": "portrait_2d",
    "name": "迪士尼卡通风",
    "cover_url": "https://cdn.maotuanzi.ai/mock/style-disney.jpg",
    "is_member_only": false,
    "price_cents": null,
    "sort_order": 20
  },
  {
    "id": "watercolor",
    "type": "portrait_2d",
    "name": "水彩手绘",
    "cover_url": "https://cdn.maotuanzi.ai/mock/style-watercolor.jpg",
    "is_member_only": true,
    "price_cents": 990,
    "sort_order": 30
  },
  {
    "id": "ancient_chinese",
    "type": "portrait_2d",
    "name": "古风汉服",
    "cover_url": "https://cdn.maotuanzi.ai/mock/style-ancient.jpg",
    "is_member_only": true,
    "price_cents": 990,
    "sort_order": 40
  },
  {
    "id": "cyberpunk",
    "type": "portrait_2d",
    "name": "赛博朋克",
    "cover_url": "https://cdn.maotuanzi.ai/mock/style-cyber.jpg",
    "is_member_only": true,
    "price_cents": 990,
    "sort_order": 50
  }
]
```

---

## 五、AI 任务（与 ai_tasks 表字段对齐）

### 5.1 任务成功

```json
{
  "task_id": "t_mock_001",
  "work_id": 300001,
  "status": 2,
  "progress": 100,
  "preview_url": "https://cdn.maotuanzi.ai/mock/preview-001-wm.jpg",
  "result_url": null,
  "extra_urls": null,
  "error_code": null,
  "error_msg": null,
  "finished_at": "2026-08-18T10:00:30Z"
}
```

### 5.2 任务处理中

```json
{
  "task_id": "t_mock_002",
  "work_id": 300002,
  "status": 1,
  "progress": 65,
  "preview_url": null,
  "result_url": null,
  "extra_urls": null,
  "error_code": null,
  "error_msg": null,
  "finished_at": null
}
```

### 5.3 任务失败

```json
{
  "task_id": "t_mock_003",
  "work_id": 300003,
  "status": 3,
  "progress": 0,
  "preview_url": null,
  "result_url": null,
  "extra_urls": null,
  "error_code": "400002",
  "error_msg": "图片分辨率过低，请上传更清晰的图片",
  "finished_at": "2026-08-18T10:01:00Z"
}
```

---

## 六、作品（与 works 表字段对齐）

### 6.1 2D 画像（未付费）

```json
{
  "id": 300001,
  "user_id": 100001,
  "pet_id": 200001,
  "type": 1,
  "style": "oil_painting",
  "status": 1,
  "preview_url": "https://cdn.maotuanzi.ai/mock/preview-001-wm.jpg",
  "result_url": null,
  "extra_urls": null,
  "is_public": 0,
  "publish_at": null,
  "like_count": 0,
  "cost_cents": 80,
  "created_at": "2026-08-18T10:00:00Z"
}
```

### 6.2 2D 画像（已付费）

```json
{
  "id": 300002,
  "user_id": 100001,
  "pet_id": 200001,
  "type": 1,
  "style": "disney",
  "status": 1,
  "preview_url": "https://cdn.maotuanzi.ai/mock/preview-002-wm.jpg",
  "result_url": "https://cdn.maotuanzi.ai/mock/result-002-hd.jpg",
  "extra_urls": null,
  "is_public": 1,
  "publish_at": "2026-08-18T11:00:00Z",
  "like_count": 23,
  "cost_cents": 80,
  "created_at": "2026-08-18T09:00:00Z"
}
```

---

## 七、商品

```json
[
  { "id": "portrait_single", "name": "高清宠物画像", "price_cents": 990, "type": "single" },
  { "id": "portrait_3d", "name": "3D 萌宠数字手办", "price_cents": 1990, "type": "single" },
  { "id": "feeding_single", "name": "专属喂养方案", "price_cents": 1990, "type": "single" },
  { "id": "breed_manual", "name": "品种深度养护手册", "price_cents": 990, "type": "single" },
  { "id": "vip_month", "name": "月卡会员", "price_cents": 1990, "type": "subscription", "duration_days": 30 },
  { "id": "vip_quarter", "name": "季卡会员", "price_cents": 4990, "type": "subscription", "duration_days": 90 },
  { "id": "vip_year", "name": "年卡会员", "price_cents": 12900, "type": "subscription", "duration_days": 365 }
]
```

---

## 八、订单

### 8.1 待支付订单

```json
{
  "id": 400001,
  "order_no": "MT20260818100001000001",
  "user_id": 100001,
  "product_type": 1,
  "product_id": "portrait_single",
  "product_name": "高清宠物画像",
  "amount_cents": 990,
  "pay_amount_cents": 990,
  "discount_cents": 0,
  "coupon_id": null,
  "status": 0,
  "expire_at": "2026-08-18T10:30:00Z",
  "created_at": "2026-08-18T10:00:00Z"
}
```

### 8.2 已支付订单

```json
{
  "id": 400002,
  "order_no": "MT20260818100002000002",
  "user_id": 100001,
  "product_type": 3,
  "product_id": "vip_month",
  "product_name": "月卡会员",
  "amount_cents": 1990,
  "pay_amount_cents": 1990,
  "discount_cents": 0,
  "coupon_id": null,
  "status": 1,
  "wx_transaction_id": "mock_txn_002",
  "paid_at": "2026-08-18T10:05:00Z",
  "created_at": "2026-08-18T10:00:00Z"
}
```

---

## 九、症状查询

```json
{
  "summary": "猫咪软便可能由饮食变化、应激或轻微肠胃不适引起",
  "common_causes": [
    "近期换粮",
    "环境变化",
    "轻微肠胃菌群失调"
  ],
  "home_care": [
    "暂停零食 24 小时",
    "少食多餐",
    "保证饮水清洁"
  ],
  "warning_signs": [
    "便血",
    "精神萎靡",
    "持续呕吐",
    "拒食超过 24 小时"
  ],
  "disclaimer": "⚠️ 本内容仅供参考，不构成兽医诊断及医疗建议。若宠物出现持续不适，请立即前往正规宠物医院就诊。",
  "related_product": {
    "id": "feeding_single",
    "name": "专属喂养方案",
    "price_cents": 1990
  }
}
```

---

## 十、喂养方案

```json
{
  "id": 500001,
  "user_id": 100001,
  "pet_id": 200001,
  "order_id": 400003,
  "plan_content": {
    "summary": "成年英短蓝猫的日常喂养方案",
    "daily_food": {
      "amount_g": 65,
      "times_per_day": 2,
      "main_food": "皇家室内成猫粮",
      "supplements": ["化毛膏每周 2 次"]
    },
    "nutrition": {
      "protein_pct": 32,
      "fat_pct": 14
    },
    "schedule": [
      { "time": "08:00", "action": "喂食" },
      { "time": "20:00", "action": "喂食" }
    ],
    "warnings": ["严格控制零食量", "避免突然换粮"],
    "disclaimer": "本方案仅供参考，具体喂养请结合宠物实际情况并咨询兽医。"
  },
  "adjust_count": 0,
  "created_at": "2026-08-18T10:00:00Z"
}
```

---

## 十一、健康提醒

```json
{
  "id": 700001,
  "user_id": 100001,
  "pet_id": 200001,
  "type": 2,
  "title": "体外驱虫",
  "cycle_days": 30,
  "next_run_at": "2026-09-18T10:00:00Z",
  "last_run_at": "2026-08-18T10:00:00Z",
  "note": "使用大宠爱滴剂",
  "is_active": 1,
  "created_at": "2026-08-18T10:00:00Z"
}
```

---

## 十二、广场帖子

```json
{
  "id": 600001,
  "user_id": 100002,
  "work_id": 300002,
  "content": "我家毛球的油画风画像，超好看！#毛团子AI",
  "image_urls": ["https://cdn.maotuanzi.ai/mock/square-001.jpg"],
  "topic": "portrait",
  "is_3d": false,
  "like_count": 23,
  "status": 1,
  "created_at": "2026-08-18T09:00:00Z"
}
```

---

## 十三、错误响应

```json
{
  "code": 101002,
  "message": "缺少必填参数",
  "data": null,
  "trace_id": "abc123"
}
```

完整错误码见 [`docs/设计/接口错误码清单.md`](../设计/接口错误码清单.md)。

---

## 十四、Mock 服务端实现建议

开发期可在 `apps/api` 中增加 Mock 中间件：

```typescript
// 开发环境启用
if (process.env.MOCK_MODE === 'true') {
  app.use('/mock', MockController);
}
```

所有 Mock 接口返回的数据应遵循本文档格式。

---

## 十五、未决问题

| # | 问题 | 优先级 |
|---|------|:---:|
| 1 | 完整品种库数据（100+）| 🔴 P0 |
| 2 | Mock 图片资源（CDN 域名）| 🟠 P1 |
| 3 | Mock 服务端实现（NestJS 中间件）| 🟠 P1 |
| 4 | 测试环境 Factory 与 Mock 数据分离 | 🟡 P2 |

---

> 🧪 Mock 数据用于本地开发和演示。**测试环境应使用 Factory 自动生成**，避免数据污染和依赖固定 ID。
