# 毛团子AI · 部署 SOP

> 版本：V2.0  · 适用：生产环境首次发布 + 后续迭代

---

## 一、部署前置

### 1.1 服务器最低配置（MVP 期）

| 项 | 配置 |
|---|---|
| CPU | 2 核 |
| 内存 | 4 GB |
| 磁盘 | 40 GB SSD |
| 带宽 | 5 Mbps |
| OS | Alibaba Cloud Linux 3 / CentOS 7+ / Ubuntu 20+ |
| Java | OpenJDK 17 |
| Node.js | 20.x LTS |
| MySQL | 8.0+ |
| Redis | 6.x+ |
| Docker | 24.x+ |
| Docker Compose | v2.x |

### 1.2 必备账号与凭证

- [ ] 阿里云账号（ECS + OSS + 内容安全）
- [ ] 微信小程序 AppID + AppSecret
- [ ] 微信支付商户号 + API v3 密钥 + 商户证书
- [ ] AI 服务商 API Key（豆包/通义万相/腾讯混元等）

---

## 二、初始化服务器

### 2.1 创建 ECS 实例

```bash
# 阿里云控制台 → ECS → 创建实例
# 规格：ecs.t6-c1m2.large（2 核 4 GB）
# 镜像：Alibaba Cloud Linux 3.2104 LTS 64 位
# 系统盘：40 GB SSD
# 公网带宽：5 Mbps 按流量
# 安全组：开放 22(SSH)/80/443/8080
```

### 2.2 安装基础环境

```bash
# SSH 登录
ssh root@<your-server-ip>

# 更新系统
yum update -y

# 安装常用工具
yum install -y git vim curl wget net-tools lsof

# 安装 Docker
curl -fsSL https://get.docker.com | sh
systemctl start docker
systemctl enable docker

# 安装 Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 安装 Java 17（用于本地构建）
yum install -y java-17-openjdk java-17-openjdk-devel
```

### 2.3 申请与配置域名

```
1. 阿里云 / 腾讯云 购买域名（如 maotuanzi.ai）
2. ICP 备案（7-20 个工作日）
3. DNS 解析：
   - api.maotuanzi.ai → ECS 公网 IP
   - admin.maotuanzi.ai → ECS 公网 IP
   - cdn.maotuanzi.ai → 阿里云 CDN 加速域名
```

### 2.4 创建数据库

```sql
-- MySQL 8.0
mysql -u root -p
CREATE DATABASE maotuanzi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER ''maotuanzi''@''%'' IDENTIFIED BY ''YourStrongPassword'';
GRANT ALL ON maotuanzi.* TO ''maotuanzi''@''%'';
FLUSH PRIVILEGES;
```

---

## 三、配置 OSS

### 3.1 创建 Bucket

```
控制台 → OSS → 创建 Bucket
- 名称：maotuanzi-prod
- 区域：华东 1（杭州）或就近
- 存储类型：标准存储
- 读写权限：私有
```

### 3.2 配置 CDN（可选）

```
OSS → Bucket → 域名管理 → 绑定加速域名
- cdn.maotuanzi.ai
- 阿里云 CDN 控制台 → 添加域名
- 源站：OSS Bucket
- 缓存策略：
  - uploads/, works/, temp/ → 不缓存（私有读 + 签名 URL）
  - square/, static/ → 缓存 30 天（公共读）
```

### 3.3 创建 RAM 子账号

```
控制台 → RAM → 用户 → 创建用户
- 名称：maotuanzi-oss
- 访问方式：OpenAPI 调用
- 权限：AliyunOSSFullAccess（生产环境缩到最小权限）
- 保存 AccessKeyId + AccessKeySecret
```

---

## 四、部署后端

### 4.1 部署代码

```bash
# 创建项目目录
mkdir -p /opt/maotuanzi
cd /opt/maotuanzi

# 上传代码（git 或 scp）
git clone https://github.com/your-org/maotuanzi.git .

# 拉取后端镜像或本地构建
cd backend

# 配置环境变量
cp src/main/resources/application.yml src/main/resources/application-prod.yml
vim src/main/resources/application-prod.yml
```

### 4.2 关键配置（生产）

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/maotuanzi?useSSL=true
    username: maotuanzi
    password: ${DB_PASSWORD}

  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD}

maotuanzi:
  jwt:
    secret: ${JWT_SECRET}     # 32+ 字符随机
  wx:
    enabled: true
    app-id: ${WECHAT_APP_ID}
    app-secret: ${WECHAT_APP_SECRET}
    pay:
      mch-id: ${WECHAT_PAY_MCH_ID}
      api-key: ${WECHAT_PAY_API_KEY}
  oss:
    enabled: true
    access-key-id: ${OSS_AK}
    access-key-secret: ${OSS_SK}
    cdn-domain: https://cdn.maotuanzi.ai
```

### 4.3 Docker Compose 编排

`docker-compose.yml`：

```yaml
version: ''3.8''
services:
  mysql:
    image: mysql:8.0
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: maotuanzi
    volumes:
      - ./db/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
      - ./db/seeds:/docker-entrypoint-initdb.d/seeds
      - mysql-data:/var/lib/mysql
    ports:
      - "3306:3306"

  redis:
    image: redis:6-alpine
    restart: always
    command: ["redis-server", "--requirepass", "${REDIS_PASSWORD}"]
    volumes:
      - redis-data:/data

  api:
    build: ./backend
    restart: always
    depends_on:
      - mysql
      - redis
    environment:
      DB_HOST: mysql
      DB_PASSWORD: ${MYSQL_PASSWORD}
      REDIS_HOST: redis
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      WECHAT_APP_ID: ${WECHAT_APP_ID}
      WECHAT_APP_SECRET: ${WECHAT_APP_SECRET}
      WECHAT_PAY_MCH_ID: ${WECHAT_PAY_MCH_ID}
      WECHAT_PAY_API_KEY: ${WECHAT_PAY_API_KEY}
      OSS_ACCESS_KEY_ID: ${OSS_AK}
      OSS_ACCESS_KEY_SECRET: ${OSS_SK}
    ports:
      - "8080:8080"

  admin:
    build: ./apps/admin
    restart: always
    depends_on:
      - api
    ports:
      - "5173:80"

volumes:
  mysql-data:
  redis-data:
```

### 4.4 启动

```bash
# 复制环境变量模板
cp .env.example .env
vim .env     # 填写生产环境变量

# 拉取 / 构建镜像
docker-compose pull
docker-compose build

# 启动
docker-compose up -d

# 查看状态
docker-compose ps
docker-compose logs -f api
```

---

## 五、部署管理后台

```bash
# 方式 A：使用 Docker（推荐）
# docker-compose.yml 中已包含 admin 服务

# 方式 B：手动构建
cd apps/admin
npm install
npm run build

# 产物在 dist/，用 Nginx 托管
cp -r dist/* /usr/share/nginx/html/admin/
```

Nginx 配置：

```nginx
server {
    listen 80;
    server_name admin.maotuanzi.ai;

    root /usr/share/nginx/html/admin;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /admin/ {
        proxy_pass http://127.0.0.1:8080/admin/;
    }
    location /v1/ {
        proxy_pass http://127.0.0.1:8080/v1/;
    }
}
```

---

## 六、配置 HTTPS

```bash
# 方式 A：阿里云 SSL 证书
1. 申请免费证书（DV SSL）
2. 下载 Nginx 证书包
3. 上传到 /etc/nginx/ssl/
4. 配置：

server {
    listen 443 ssl;
    server_name api.maotuanzi.ai;

    ssl_certificate /etc/nginx/ssl/public.crt;
    ssl_certificate_key /etc/nginx/ssl/private.key;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# 方式 B：Let''s Encrypt（免费，自动续签）
yum install -y certbot python-certbot-nginx
certbot --nginx -d api.maotuanzi.ai -d admin.maotuanzi.ai
```

---

## 七、配置定时任务

```bash
# Cron 任务（机器学习 + 数据备份）
crontab -e

# 每天凌晨 2 点：全量数据库备份
0 2 * * * /opt/maotuanzi/scripts/backup-db.sh

# 每天凌晨 3 点：清理临时 OSS 文件
0 3 * * * /opt/maotuanzi/scripts/cleanup-oss.sh

# 每 5 分钟：日志归档
*/5 * * * * /usr/local/bin/logrotate /opt/maotuanzi/logs/*.log
```

---

## 八、上线检查

参考 [`./上线检查清单.md`](./上线检查清单.md)

---

## 九、监控告警

- **健康检查**：`curl https://api.maotuanzi.ai/v1/health`
- **Prometheus**：后端 `/actuator/prometheus`
- **日志聚合**：阿里云 SLS（日志服务）
- **告警渠道**：钉钉/企业微信机器人 + 短信

---

## 十、回滚预案

```bash
# 1. 停止当前版本
docker-compose stop api admin

# 2. 回退到上一个镜像 tag
docker-compose pull
docker-compose up -d

# 3. 数据库回滚（Flyway）
mysqldump -u root -p maotuanzi > backup_before_rollback.sql
mysql -u root -p maotuanzi < /opt/maotuanzi/backups/db_20260815.sql

# 4. 验证
curl https://api.maotuanzi.ai/v1/health
```

---

## 十一、密钥轮换

| 密钥 | 周期 | 负责人 |
|------|------|--------|
| `JWT_SECRET` | 90 天 | 技术 |
| `WECHAT_APP_SECRET` | 90 天 | 技术 |
| `WECHAT_PAY_API_KEY` | 365 天 | 财务 |
| `OSS_AK/SK` | 180 天 | 技术 |
| AI Provider Key | 90 天 | 技术 |

---

## 十二、版本升级 SOP

```bash
# 1. 拉取新版本代码
cd /opt/maotuanzi
git pull origin main

# 2. 数据库迁移（如果有）
docker-compose exec mysql mysql -u root -p maotuanzi < db/migrations/V003__xxx.sql

# 3. 重新构建并滚动重启
docker-compose build api admin
docker-compose up -d --no-deps api admin

# 4. 验证
curl https://api.maotuanzi.ai/v1/health
curl https://admin.maotuanzi.ai
```

---

> ⚠️ 生产部署务必在测试环境验证至少 24 小时后再上线。
> 🚀 部署完成后第一时间在 [`./上线检查清单.md`](./上线检查清单.md) 上勾选所有项。