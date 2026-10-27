# 毛团子AI · 第三方服务接入指引

> 版本：V2.0 · 适用：所有第三方服务的接入步骤

---

## 一、微信小程序 + 微信支付

### 1.1 准备工作

| 项 | 申请地址 |
|---|---|
| 小程序 AppID | https://mp.weixin.qq.com |
| 商户号 | https://pay.weixin.qq.com |
| 开放平台账号（UnionID） | https://open.weixin.qq.com |

### 1.2 配置

`.env`：
```
WECHAT_APP_ID=wx_xxx
WECHAT_APP_SECRET=xxx
WECHAT_PAY_MCH_ID=xxx
WECHAT_PAY_API_KEY=32位密钥
WECHAT_PAY_NOTIFY_URL=https://api.maotuanzi.ai/v1/webhooks/wx-pay
```

### 1.3 服务类目

推荐：「工具 → 效率」（低风险）
备选：「工具 → 图像处理」

避免：「宠物医疗」（需特殊资质）

### 1.4 一键登录接入

小程序端：
```typescript
wx.login({
  success: ({ code }) => api.post('/v1/auth/wx-login', { code })
});
```

服务端：调用 `/sns/jscode2session` 换 openid → 返回 JWT

### 1.5 支付接入

支付流程：
1. 创建订单 → 后端返回 prepay_id
2. `wx.requestPayment` 唤起支付
3. 用户输入密码
4. 微信回调 `/v1/webhooks/wx-pay`
5. 后端验签 + 更新订单 + 发权益（幂等）

退款：调用 `/v3/refund/domestic/refunds`

---

## 二、阿里云 OSS

### 2.1 创建 Bucket

```
1. 控制台 → OSS → 创建 Bucket
2. 名称：maotuanzi-prod
3. 区域：oss-cn-hangzhou（华东1）或就近
4. 存储类型：标准存储
5. 读写权限：私有
```

### 2.2 跨域配置

Bucket → 跨域设置 → 创建规则：
- 来源：`*`
- 允许 Methods：GET, POST, PUT, DELETE, HEAD
- 允许 Headers：`*`
- 暴露 Headers：ETag, x-oss-request-id

### 2.3 创建 RAM 子账号

```
控制台 → RAM → 用户 → 创建用户
访问方式：OpenAPI
权限：AliyunOSSFullAccess（生产缩到最小）
```

### 2.4 CDN 加速（可选）

```
OSS → 域名管理 → 绑定加速域名
阿里云 CDN → 添加域名
缓存策略：
  - uploads/, works/, temp/ → 不缓存
  - square/, static/ → 缓存 30 天
```

---

## 三、AI 服务商

### 3.1 选型（待实测）

| 服务 | 用途 | 优先级 |
|---|---|---|
| 豆包大模型 | 文本生成（症状/喂养） | 🔴 P0 |
| 通义万相 | 图生图（2D 画像） | 🔴 P0 |
| Tripo3D | 单图生成 3D | 🟠 P1 |
| 百度 AI | 宠物品种识别 | 🟠 P1 |

### 3.2 Mock 模式

`.env` 设置 `AI_MOCK_MODE=true` 时：
- AI 任务立即返回成功（延迟 3-6 秒模拟）
- 生成结果 URL 指向 mock CDN

### 3.3 接入真实 AI 网关（建议）

新建独立 Python 服务（FastAPI）：

```python
# ai_gateway/main.py
from fastapi import FastAPI
import httpx

app = FastAPI()

@app.post("/v1/portrait")
async def portrait(prompt: str, image_url: str):
    # 调用豆包/通义万相
    async with httpx.AsyncClient() as client:
        resp = await client.post(
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis",
            headers={"Authorization": f"Bearer {DASHSCOPE_API_KEY}"},
            json={"model": "wanx-v1", "input": {"prompt": prompt, "image_url": image_url}}
        )
        return resp.json()
```

主后端调用 AI 网关：
```java
// AiServiceImpl
String url = "http://ai-gateway:8000/v1/portrait";
HttpResponse resp = HttpUtil.post(url, params);
```

---

## 四、短信 / 邮件

### 4.1 阿里云短信

用途：验证码 / 告警

```java
Dysmsapi client = new Dysmsapi(new Config()
    .setAccessKeyId(ALIYUN_ACCESS_KEY_ID)
    .setAccessKeySecret(ALIYUN_ACCESS_KEY_SECRET));

SendSmsRequest req = new SendSmsRequest()
    .setPhoneNumbers("13800000000")
    .setSignName("毛团子AI")
    .setTemplateCode("SMS_xxx")
    .setTemplateParam("{\"code\":\"123456\"}");
```

### 4.2 阿里云邮件推送

用途：内部告警、运营通知

---

## 五、监控告警

### 5.1 Sentry（错误监控）

```yaml
maotuanzi:
  sentry:
    dsn: https://xxx@sentry.io/xxx
    environment: production
    traces-sample-rate: 0.1
```

### 5.2 阿里云 ARMS（性能监控）

接入 Spring Boot：
```xml
<dependency>
    <groupId>com.aliyun.arms</groupId>
    <artifactId>arms-agent</artifactId>
</dependency>
```

### 5.3 UptimeRobot（可用性）

监控 `/v1/health`，5 分钟间隔，失败告警邮件。

---

## 六、客服系统

### 6.1 MVP 方案（推荐）

直接用微信小程序内置客服组件：
```xml
<button open-type="contact" bindcontact="handleContact">联系客服</button>
```

后台：微信小程序后台 → 客服 → 微信客服

### 6.2 进阶方案

- 美洽（免费版）
- 智齿客服
- 网易七鱼

---

## 七、密钥管理

### 7.1 分级

| 级别 | 例子 | 存储 |
|---|---|---|
| 🔴 核心 | 微信支付 API v3、商户私钥 | 阿里云 KMS |
| 🟠 重要 | 阿里云 OSS、阿里云 SMS | 环境变量 + KMS |
| 🟡 一般 | Sentry、UptimeRobot | 环境变量 |
| 🟢 公开 | AI Provider Key | 环境变量 |

### 7.2 轮换周期

| 类型 | 周期 |
|---|---|
| 微信 AppSecret | 90 天 |
| 微信支付 API v3 | 365 天 |
| OSS AccessKey | 180 天 |
| AI Provider Key | 90 天 |

### 7.3 安全规范

- ❌ 禁止把密钥 commit 到代码
- ❌ 禁止把生产密钥共享到非生产环境
- ✅ 必须使用 KMS 管理核心密钥
- ✅ 在 `.env.example` 提供模板（不放真实值）
- ✅ 在 CI 中启用密钥泄露扫描

---

## 八、对接 Checklist

新增第三方服务时：
- [ ] 申请账号 + 实名认证
- [ ] 创建独立子账号 / RAM
- [ ] 配置最小权限
- [ ] 获取凭证 + 存储到 KMS
- [ ] 沙箱环境测试
- [ ] 生产环境切换
- [ ] 配置告警
- [ ] 配置降级方案
- [ ] 文档记录
- [ ] 季度审计权限