# 毛团子AI · 监控告警 SOP

**版本**：V1.0 | **更新**：2026-08-18

> 监控指标阈值需根据实测调整，**本文档为框架**。

---

## 一、监控工具链

| 类型 | 工具 |
|------|------|
| 错误监控 | Sentry（前后端） |
| 指标采集 | Prometheus |
| 可视化 | Grafana |
| 日志 | Loki / 阿里云 SLS |
| 可用性 | UptimeRobot |

## 二、关键指标（阈值待实测）

### 2.1 业务指标

| 指标 | 警告 | 危险 |
|------|------|------|
| DAU 日环比 | -30% | -50% |
| 付费转化率 | < 5% | < 3% |
| 退款率（周环比）| +50% | +100% |
| AI 任务成功率 | < 90% | < 80% |

### 2.2 应用指标

| 指标 | 警告 | 危险 |
|------|------|------|
| API P99 延迟 | > 1s | > 3s |
| API 错误率 | > 2% | > 5% |
| 微信支付成功率 | < 98% | < 95% |

### 2.3 系统指标

| 指标 | 警告 | 危险 |
|------|------|------|
| CPU 使用率 | > 70% 持续 5min | > 85% |
| 内存使用率 | > 80% | > 90% |
| 磁盘使用率 | > 75% | > 85% |
| 数据库慢查询占比 | > 3% | > 5% |

## 三、告警分级与响应

### 3.1 P0（服务不可用）

**响应时效**：5 分钟内拉群，30 分钟内决策

**触发示例**：
- `/health` 接口连续 3 次失败
- API 错误率 > 50% 持续 1 分钟
- 数据库连接耗尽

**响应 SOP**：

```bash
# 1. 拉群（@创始人 + 后端）
# 2. 拉日志
ssh deploy@prod-server "docker compose logs api --tail=200 | grep ERROR"

# 3. 检查 AI 服务
curl -I https://api.doubao.com/health

# 4. 决定：修复 / 回滚 / 切备用
./deploy.sh <previous-tag>

# 5. 通知用户（必要时）
```

### 3.2 P1（核心功能降级）

**响应时效**：30 分钟内处理

**典型场景**：
- AI 服务商故障 → 切备用
- 微信支付回调失败 → 检查商户号

### 3.3 P2（性能下降）

**响应时效**：4 小时内处理

**典型场景**：
- API 延迟升高
- 数据库慢查询增多

## 四、值班表

值班制度：

- 排班：每周轮换
- 时段：全天 24h（含节假日）
- 响应：P0 5min / P1 30min

> 值班表独立维护，不在本文档。

## 五、Prometheus 告警规则

```yaml
# /opt/maotuanzi/monitoring/alerts.yml
groups:
  - name: api
    rules:
      - alert: HighErrorRate
        expr: |
          sum(rate(http_requests_total{status=~"5.."}[5m])) by (service)
          / sum(rate(http_requests_total[5m])) by (service) > 0.05
        for: 5m
        labels: { severity: critical }
        annotations:
          summary: "{{ $labels.service }} 错误率 > 5%"

      - alert: HighLatency
        expr: |
          histogram_quantile(0.99,
            sum(rate(http_request_duration_seconds_bucket[5m])) by (le, service)
          ) > 3
        for: 5m
        labels: { severity: warning }
        annotations:
          summary: "{{ $labels.service }} P99 > 3s"

  - name: ai
    rules:
      - alert: AITaskHighFailure
        expr: |
          sum(rate(ai_task_failed_total[5m]))
          / sum(rate(ai_task_total[5m])) > 0.15
        for: 5m
        labels: { severity: critical }
        annotations:
          summary: "AI 任务失败率 > 15%"

  - name: system
    rules:
      - alert: HighCPU
        expr: 100 - (avg by(instance)(rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
        for: 5m
        labels: { severity: warning }

      - alert: DiskSpaceLow
        expr: (node_filesystem_avail_bytes / node_filesystem_size_bytes) * 100 < 15
        for: 5m
        labels: { severity: warning }
```

## 六、复盘机制

P0 / P1 事件后必须输出复盘报告：

```
- 事件概述（时间、影响、等级）
- 时间线（每 10 分钟节点）
- 根因分析（5 Why）
- 应急措施回顾
- Action Items（负责人 + 截止时间）
```

详见 [`/docs/设计/08-风险评估与合规预案.md`](../设计/08-风险评估与合规预案.md) § 七。

## 七、未决问题

| # | 问题 | 优先级 |
|---|------|:---:|
| 1 | 真实阈值（需实测）| 🔴 P0 |
| 2 | Grafana 看板布局 | 🟠 P1 |
| 3 | 值班联系人独立维护 | 🟠 P1 |
| 4 | 告警通道稳定性测试 | 🟡 P2 |

---

> 📟 **告警阈值需根据 MVP 实际数据调整**。