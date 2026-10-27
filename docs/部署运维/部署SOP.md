# 毛团子AI · 部署 SOP

**版本**：V1.0 | **更新**：2026-08-18

> 部署通过 [`/.github/workflows/deploy.yml`](../../.github/workflows/deploy.yml) 自动执行。本文档定义**手动部署、回滚、应急**流程。

---

## 一、环境清单

| 环境 | 地址 | 数据库 | 触发 |
|------|------|--------|------|
| dev | localhost | 共享 docker | 本地 |
| staging | staging.maotuanzi.ai | 独立 | push main |
| production | maotuanzi.ai | RDS 主从 | git tag v* |

## 二、首次部署

```bash
# 1. 准备服务器
ssh deploy@prod-server
sudo apt update && sudo apt install -y docker.io docker-compose-plugin

# 2. 创建部署目录
sudo mkdir -p /opt/maotuanzi
sudo chown -R deploy:deploy /opt/maotuanzi

# 3. 部署脚本
cat > /opt/maotuanzi/deploy.sh << 'EOF'
#!/bin/bash
set -e
cd /opt/maotuanzi

# 拉取镜像
docker pull registry.cn-hangzhou.aliyuncs.com/maotuanzi/api:${1:-latest}
docker pull registry.cn-hangzhou.aliyuncs.com/maotuanzi/ai-gateway:${1:-latest}

# 备份当前镜像 tag
echo "$(docker inspect --format='{{.Image}}' maotuanzi-api 2>/dev/null || echo none)" > .rollback-api
echo "$(docker inspect --format='{{.Image}}' maotuanzi-ai-gateway 2>/dev/null || echo none)" > .rollback-ai

# 启动
docker compose -f docker-compose.prod.yml up -d

# 健康检查
sleep 30
curl -fsS http://localhost:3000/health
echo "✅ 部署完成"
EOF
chmod +x /opt/maotuanzi/deploy.sh

# 4. 配置环境变量（从 KMS）
node scripts/fetch-secrets.js production
```

## 三、日常发布

通过 GitHub Actions 自动：

```
PR merged to develop → 自动部署 staging
git tag v1.2.3 → 手动确认后部署 production
```

详见 [`/.github/workflows/deploy.yml`](../../.github/workflows/deploy.yml)。

## 四、回滚

### 4.1 自动回滚

`deploy.yml` 在健康检查失败时自动回滚到 `.rollback-api` / `.rollback-ai` 记录的镜像。

### 4.2 手动回滚

```bash
ssh deploy@prod-server
cd /opt/maotuanzi

# 查看最近 10 个镜像
docker images | head -10

# 选择上一个 tag 回滚
./deploy.sh <previous-tag>

# 或紧急回滚到上一个稳定版本
TAG=$(cat .rollback-api | cut -d: -f2)
./deploy.sh $TAG
```

### 4.3 数据库回滚

```bash
# 1. 停止应用
docker compose stop api

# 2. 从备份恢复（详见 § 六 备份）
gunzip < /backup/mysql/maotuanzi-20260818.sql.gz | \
  docker compose exec -T mysql mysql -uroot -p maotuanzi

# 3. 重启
docker compose up -d api
```

## 五、密钥轮换

详见 [`/docs/开发/环境变量说明.md`](../开发/环境变量说明.md) § 八。

## 六、备份

### 6.1 备份策略

| 备份 | 频率 | 保留 |
|------|------|------|
| MySQL 全量 | 每日 03:00 | 30 天 |
| binlog | 实时 | 7 天 |
| OSS 跨区复制 | 每小时 | 30 天 |

### 6.2 备份脚本

```bash
# /opt/maotuanzi/scripts/backup.sh
#!/bin/bash
set -e
DATE=$(date +%Y%m%d-%H%M%S)
BACKUP_DIR=/backup/mysql

mkdir -p $BACKUP_DIR
docker compose exec -T mysql mysqldump -uroot -p"$DB_PASSWORD" \
  --single-transaction --routines --triggers \
  maotuanzi | gzip > $BACKUP_DIR/maotuanzi-${DATE}.sql.gz

# 清理 30 天前
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete
```

加入 crontab：

```bash
0 3 * * * /opt/maotuanzi/scripts/backup.sh
```

## 七、应急

| 情况 | 操作 |
|------|------|
| 服务不可用 | 见 [`监控告警 SOP`](监控告警SOP.md) § 三 |
| 数据库丢失 | § 4.3 回滚 + 备份恢复 |
| 误发布 | git revert + 重新部署 |
| 安全事件 | 见 [`08-风险评估与合规预案`](../设计/08-风险评估与合规预案.md) § 三 |

## 八、未决问题

| # | 问题 | 优先级 |
|---|------|:---:|
| 1 | 服务器规格（CPU/内存/带宽）| 🔴 P0 |
| 2 | 备份异地复制（OSS）| 🟠 P1 |
| 3 | 数据库连接池大小 | 🟠 P1 |
| 4 | 灰度发布比例 | 🟡 P2 |

---

> 🚀 **生产部署必须先在 staging 验证**。