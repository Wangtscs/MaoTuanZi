package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.entity.OpsAlarmRecord;
import com.maotuanzi.entity.OpsInstance;
import com.maotuanzi.entity.SystemConfig;
import com.maotuanzi.mapper.OpsMapper;
import com.maotuanzi.ops.OpsHttpMetrics;
import com.maotuanzi.ops.OpsInstanceInfo;
import com.maotuanzi.ops.OpsLogAppender;
import com.maotuanzi.ops.OpsLogBuffer;
import com.maotuanzi.ops.OpsMetricRegistry;
import com.maotuanzi.ops.OpsSystemMetrics;
import com.maotuanzi.ops.OpsTimeUtil;
import com.maotuanzi.ops.OpsConfigHistory;
import org.springframework.transaction.annotation.Transactional;
import com.maotuanzi.service.OpsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运维平台核心服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpsServiceImpl implements OpsService {

    private static final String[] REDIS_KEY_PREFIXES = {
            "user:token:", "admin:token:", "rate:", "idem:", "cache:", "ai:task:", "lock:"
    };

    private final OpsMapper opsMapper;
    private final OpsHttpMetrics httpMetrics;
    private final OpsSystemMetrics systemMetrics;
    private final OpsMetricRegistry metricRegistry;
    private final OpsLogBuffer logBuffer;
    private final OpsLogAppender logAppender;
    private final OpsInstanceInfo instanceInfo;
    private final OpsConfigHistory configHistory;

    @Value("${spring.datasource.url:}")
    private String jdbcUrl;

    // ==================== 总览 ====================

    @Override
    public Map<String, Object> overview() {
        Map<String, Object> m = new LinkedHashMap<>();
        Map<String, Double> metrics = metricRegistry.collect();

        // 服务信息
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("appName", instanceInfo.getAppName());
        app.put("profile", instanceInfo.getProfile());
        app.put("version", instanceInfo.getVersion());
        app.put("instanceId", instanceInfo.getInstanceId());
        app.put("host", instanceInfo.getHost());
        app.put("ip", instanceInfo.getIp());
        app.put("port", instanceInfo.getPort());
        app.put("pid", instanceInfo.getPid());
        app.put("startedAt", OpsTimeUtil.format(instanceInfo.getStartedAt()));
        app.put("uptimeMs", ManagementFactory.getRuntimeMXBean().getUptime());
        app.put("javaVersion", System.getProperty("java.version"));
        m.put("app", app);

        // 核心指标卡片
        Map<String, Object> core = new LinkedHashMap<>();
        core.put("qps", metrics.get("http.qps"));
        core.put("errorRate5m", metrics.get("http.error.rate.5m"));
        core.put("p95", metrics.get("http.p95.5m"));
        core.put("avgMs", metrics.get("http.avg.5m"));
        core.put("heapUsedPct", metrics.get("jvm.heap.used.pct"));
        core.put("cpuLoad", metrics.get("system.cpu.load"));
        core.put("memUsedPct", metrics.get("system.mem.used.pct"));
        core.put("diskFreePct", metrics.get("system.disk.free.pct"));
        core.put("threadCount", metrics.get("jvm.thread.count"));
        core.put("dbPoolUsedPct", metrics.get("db.pool.used.pct"));
        core.put("redisAvailable", metrics.get("redis.available"));
        core.put("errorLog5m", metrics.get("log.error.count.5m"));
        m.put("core", core);
        m.put("httpSummary", httpMetrics.summary());

        // 健康度
        m.put("health", health());

        // 告警
        Map<String, Object> alarm = new LinkedHashMap<>();
        try {
            Map<String, Object> stat = opsMapper.selectAlarmStat(LocalDateTime.now().minusDays(7));
            alarm.put("stat", stat == null ? Collections.emptyMap() : stat);
            List<OpsAlarmRecord> firing = opsMapper.selectAlarmRecords("FIRING", null, null, null, null, 0, 5);
            alarm.put("firingList", firing);
        } catch (Throwable t) {
            alarm.put("error", t.getMessage());
        }
        m.put("alarm", alarm);

        // 任务
        try {
            m.put("job", opsMapper.selectJobStat(null));
        } catch (Throwable t) {
            m.put("job", Collections.emptyMap());
        }

        // 业务水位
        Map<String, Object> biz = new LinkedHashMap<>();
        try {
            LocalDateTime dayAgo = LocalDateTime.now().minusHours(24);
            biz.put("newUsers24h", nvl(opsMapper.countNewUsers(dayAgo)));
            biz.put("paidOrders24h", nvl(opsMapper.countPaidOrders(dayAgo)));
            biz.put("aiTasks24h", nvl(opsMapper.countAiTasks(null, dayAgo)));
            biz.put("aiTaskPending", nvl(opsMapper.countAiTasks(0, null)));
            biz.put("aiTaskFailed24h", nvl(opsMapper.countAiTasks(3, dayAgo)));
            biz.put("pendingWorks", nvl(opsMapper.countPendingWorks()));
            biz.put("pendingRefunds", nvl(opsMapper.countPendingRefunds()));
            biz.put("pendingReports", nvl(opsMapper.countPendingReports()));
        } catch (Throwable t) {
            biz.put("error", t.getMessage());
        }
        m.put("biz", biz);

        // 实例
        m.put("instances", instances());
        m.put("logStatLevels", logBuffer.stat().get("levels"));
        m.put("serverTime", OpsTimeUtil.format(LocalDateTime.now()));
        return m;
    }

    @Override
    public Map<String, Object> health() {
        Map<String, Object> m = new LinkedHashMap<>();
        List<Map<String, Object>> checks = new ArrayList<>();
        Map<String, Double> metrics = metricRegistry.collect();

        checks.add(pingCheck("database", systemMetrics::dbPing));
        checks.add(pingCheck("redis", systemMetrics::redis));

        checks.add(metricCheck("disk", metrics.get("system.disk.free.pct"), 15, true));
        checks.add(metricCheck("jvmHeap", metrics.get("jvm.heap.used.pct"), 90, false));
        checks.add(metricCheck("dbPool", metrics.get("db.pool.used.pct"), 90, false));
        checks.add(metricCheck("httpError", metrics.get("http.error.rate.5m"), 5, false));
        Map<String, Object> infrastructure = healthPartition(checks);
        List<Map<String, Object>> business = new ArrayList<>();
        business.add(metricCheck("aiPending", metrics.get("ai.task.pending.count"), 50, false));
        business.add(metricCheck("aiFailureRate1h", metrics.get("ai.task.fail.rate.1h"), 20, false));
        m.put("infrastructure", infrastructure);
        m.put("business", healthPartition(business));
        List<Map<String, Object>> all = new ArrayList<>(checks);
        all.addAll(business);
        m.putAll(healthPartition(all));
        m.put("timestamp", OpsTimeUtil.format(LocalDateTime.now()));
        return m;
    }

    private Map<String, Object> metricCheck(String name, Double value, double threshold, boolean minimum) {
        if (value == null || !Double.isFinite(value)) {
            Map<String, Object> c = check(name, false, "指标未知，采集不可用或没有样本");
            c.put("ok", null);
            c.put("status", "UNKNOWN");
            return c;
        }
        Map<String, Object> c = check(name, minimum ? value >= threshold : value < threshold, "当前值 " + value);
        c.put("status", Boolean.TRUE.equals(c.get("ok")) ? "UP" : "DEGRADED");
        return c;
    }

    private Map<String, Object> pingCheck(String name, java.util.function.Supplier<Map<String, Object>> probe) {
        try {
            Map<String, Object> r = probe.get();
            if (r == null || !(r.get("ok") instanceof Boolean)) return metricCheck(name, null, 0, false);
            Map<String, Object> c = check(name, Boolean.TRUE.equals(r.get("ok")),
                    Boolean.TRUE.equals(r.get("ok")) ? "延迟 " + r.get("latencyMs") + "ms" : "依赖不可用");
            c.put("status", Boolean.TRUE.equals(r.get("ok")) ? "UP" : "DOWN");
            return c;
        } catch (Exception e) { return metricCheck(name, null, 0, false); }
    }

    private Map<String, Object> healthPartition(List<Map<String, Object>> checks) {
        long unknown = checks.stream().filter(c -> c.get("ok") == null).count();
        long failed = checks.stream().filter(c -> Boolean.FALSE.equals(c.get("ok"))).count();
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("status", failed > 0 ? "DEGRADED" : unknown > 0 ? "UNKNOWN" : "UP");
        p.put("score", unknown > 0 ? null : (int) Math.max(0, 100 - failed * 100.0 / checks.size()));
        p.put("checks", checks);
        p.put("timestamp", OpsTimeUtil.format(LocalDateTime.now()));
        return p;
    }

    private Map<String, Object> check(String name, boolean ok, String detail) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("name", name);
        c.put("ok", ok);
        c.put("detail", detail);
        return c;
    }

    // ==================== 实时监控 ====================

    @Override
    public Map<String, Object> realtime() {
        Map<String, Double> values = metricRegistry.collect();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, Double> e : values.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", e.getKey());
            item.put("name", OpsMetricRegistry.nameOf(e.getKey()));
            item.put("unit", OpsMetricRegistry.unitOf(e.getKey()));
            item.put("value", e.getValue());
            item.put("status", e.getValue() == null ? "UNKNOWN" : "AVAILABLE");
            list.add(item);
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("values", values);
        m.put("metrics", list);
        m.put("timeline", httpMetrics.timeline(120));
        m.put("statusDistribution", httpMetrics.statusDistribution());
        m.put("window1m", httpMetrics.window(60).toMap());
        m.put("window5m", httpMetrics.window(300).toMap());
        m.put("window1h", httpMetrics.window(3600).toMap());
        m.put("slowRequests", httpMetrics.slowSamples(20));
        m.put("serverTime", System.currentTimeMillis());
        return m;
    }

    @Override
    public List<Map<String, Object>> metricCatalog() {
        return metricRegistry.catalog();
    }

    @Override
    public Map<String, Object> httpTimeline(int seconds) {
        int s = seconds <= 0 ? 120 : Math.min(seconds, 3600);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("seconds", s);
        m.put("timeline", httpMetrics.timeline(s));
        m.put("window", httpMetrics.window(s).toMap());
        return m;
    }

    @Override
    public Map<String, Object> httpEndpoints(String orderBy, int limit) {
        int l = limit <= 0 ? 20 : Math.min(limit, 200);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orderBy", orderBy == null ? "count" : orderBy);
        m.put("list", httpMetrics.topEndpoints(orderBy, l));
        m.put("summary", httpMetrics.summary());
        m.put("statusDistribution", httpMetrics.statusDistribution());
        m.put("slowRequests", httpMetrics.slowSamples(50));
        return m;
    }

    @Override
    public Map<String, Object> metricSeries(List<String> keys, int minutes) {
        int mins = minutes <= 0 ? 60 : Math.min(minutes, 60 * 24 * 7);
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusMinutes(mins);
        List<String> queryKeys = (keys == null || keys.isEmpty())
                ? Arrays.asList("http.qps", "jvm.heap.used.pct", "system.cpu.load", "http.error.rate.5m")
                : keys;
        List<Map<String, Object>> rows;
        try {
            rows = opsMapper.selectSnapshots(queryKeys, from, to);
        } catch (Throwable t) {
            rows = Collections.emptyList();
        }

        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (String k : queryKeys) grouped.put(k, new ArrayList<>());
        for (Map<String, Object> row : rows) {
            String key = String.valueOf(row.get("metricKey"));
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        List<Map<String, Object>> series = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> e : grouped.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", e.getKey());
            item.put("name", OpsMetricRegistry.nameOf(e.getKey()));
            item.put("unit", OpsMetricRegistry.unitOf(e.getKey()));
            item.put("points", e.getValue());
            series.add(item);
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("minutes", mins);
        m.put("from", OpsTimeUtil.format(from));
        m.put("to", OpsTimeUtil.format(to));
        m.put("series", series);
        return m;
    }

    @Override
    public List<Map<String, Object>> instances() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            List<OpsInstance> instances = opsMapper.selectInstances();
            LocalDateTime now = LocalDateTime.now();
            for (OpsInstance i : instances) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("instanceId", i.getInstanceId());
                m.put("appName", i.getAppName());
                m.put("profile", i.getProfile());
                m.put("host", i.getHost());
                m.put("ip", i.getIp());
                m.put("port", i.getPort());
                m.put("pid", i.getPid());
                m.put("version", i.getVersion());
                m.put("cpuLoad", i.getCpuLoad());
                m.put("heapUsedPct", i.getHeapUsedPct());
                m.put("heapUsedMb", i.getHeapUsedMb());
                m.put("heapMaxMb", i.getHeapMaxMb());
                m.put("threadCount", i.getThreadCount());
                m.put("qps", i.getQps());
                m.put("errorRate", i.getErrorRate());
                m.put("startedAt", OpsTimeUtil.format(i.getStartedAt()));
                m.put("lastHeartbeatAt", OpsTimeUtil.format(i.getLastHeartbeatAt()));
                boolean alive = i.getLastHeartbeatAt() != null
                        && java.time.Duration.between(i.getLastHeartbeatAt(), now).getSeconds() < 60;
                m.put("status", alive ? i.getStatus() : "DOWN");
                m.put("self", instanceInfo.getInstanceId().equals(i.getInstanceId()));
                list.add(m);
            }
        } catch (Throwable t) {
            log.debug("[OPS] 实例列表查询失败: {}", t.getMessage());
        }
        return list;
    }

    @Override
    public void resetHttpMetrics() {
        httpMetrics.reset();
        log.info("[OPS] 进程内 HTTP 指标已重置");
    }

    // ==================== 系统资源 ====================

    @Override
    public Map<String, Object> systemResource() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("jvm", systemMetrics.jvm());
        m.put("os", systemMetrics.os());
        m.put("disk", systemMetrics.disk());
        m.put("dbPool", systemMetrics.dbPool());
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("appName", instanceInfo.getAppName());
        app.put("profile", instanceInfo.getProfile());
        app.put("version", instanceInfo.getVersion());
        app.put("instanceId", instanceInfo.getInstanceId());
        app.put("startedAt", OpsTimeUtil.format(instanceInfo.getStartedAt()));
        m.put("app", app);
        return m;
    }

    @Override
    public Map<String, Object> threadDump(String state, int limit) {
        int l = limit <= 0 ? 100 : Math.min(limit, 500);
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] infos = bean.dumpAllThreads(false, false);
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Integer> stateCount = new LinkedHashMap<>();
        for (ThreadInfo info : infos) {
            if (info == null) continue;
            String st = info.getThreadState().name();
            stateCount.merge(st, 1, Integer::sum);
            if (state != null && !state.isEmpty() && !state.equalsIgnoreCase(st)) continue;
            if (list.size() >= l) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", info.getThreadId());
            m.put("name", info.getThreadName());
            m.put("state", st);
            m.put("blockedCount", info.getBlockedCount());
            m.put("blockedTimeMs", info.getBlockedTime());
            m.put("waitedCount", info.getWaitedCount());
            m.put("lockName", info.getLockName());
            m.put("lockOwnerName", info.getLockOwnerName());
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] stack = info.getStackTrace();
            int max = Math.min(stack.length, 15);
            for (int i = 0; i < max; i++) {
                sb.append(stack[i].toString()).append('\n');
            }
            m.put("stack", sb.toString());
            list.add(m);
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("total", infos.length);
        r.put("stateCount", stateCount);
        r.put("list", list);
        long[] deadlocks = bean.findDeadlockedThreads();
        r.put("deadlocks", deadlocks == null ? Collections.emptyList()
                : Arrays.stream(deadlocks).boxed().collect(java.util.stream.Collectors.toList()));
        return r;
    }

    // ==================== 日志中心 ====================

    @Override
    public Map<String, Object> queryLogs(String level, String keyword, String logger,
                                         String traceId, Long sinceId, int limit) {
        int l = limit <= 0 ? 200 : Math.min(limit, 1000);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("list", logBuffer.query(level, keyword, logger, traceId, sinceId, l));
        m.put("latestId", logBuffer.latestId());
        m.put("captureLevel", logAppender.getThreshold());
        return m;
    }

    @Override
    public Map<String, Object> logStat() {
        Map<String, Object> m = logBuffer.stat();
        m.put("captureLevel", logAppender.getThreshold());
        return m;
    }

    @Override
    public void clearLogs() {
        logBuffer.clear();
        log.info("[OPS] 日志缓冲已清空");
    }

    @Override
    public Map<String, Object> setLogLevel(String level) {
        if (level == null || !Arrays.asList("ERROR", "WARN", "INFO", "DEBUG", "TRACE")
                .contains(level.toUpperCase())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "级别仅支持 ERROR/WARN/INFO/DEBUG/TRACE");
        }
        logAppender.setThreshold(level.toUpperCase());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("captureLevel", logAppender.getThreshold());
        return m;
    }

    // ==================== 数据源 ====================

    @Override
    public Map<String, Object> dbStatus() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("pool", systemMetrics.dbPool());
        m.put("ping", systemMetrics.dbPing());
        m.put("schema", currentSchema());
        return m;
    }

    @Override
    public List<Map<String, Object>> dbTables() {
        String schema = currentSchema();
        if (schema == null) return Collections.emptyList();
        try {
            List<Map<String, Object>> rows = opsMapper.selectTableStats(schema);
            for (Map<String, Object> row : rows) {
                long data = toLong(row.get("dataLength"));
                long index = toLong(row.get("indexLength"));
                row.put("dataMb", round2(data / 1024.0 / 1024));
                row.put("indexMb", round2(index / 1024.0 / 1024));
                row.put("totalMb", round2((data + index) / 1024.0 / 1024));
                row.put("freeMb", round2(toLong(row.get("dataFree")) / 1024.0 / 1024));
            }
            return rows;
        } catch (Throwable t) {
            log.debug("[OPS] 表统计查询失败: {}", t.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> redisStatus() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("info", systemMetrics.redis());
        m.put("keyGroups", systemMetrics.redisKeyGroups(REDIS_KEY_PREFIXES));
        return m;
    }

    private String currentSchema() {
        try {
            if (jdbcUrl == null || jdbcUrl.isEmpty()) return null;
            String s = jdbcUrl.substring(jdbcUrl.lastIndexOf('/') + 1);
            int q = s.indexOf('?');
            return q > 0 ? s.substring(0, q) : s;
        } catch (Throwable t) {
            return null;
        }
    }

    // ==================== 存储 ====================

    @Override
    public Map<String, Object> storageOverview(int days) {
        int d = days <= 0 ? 7 : Math.min(days, 90);
        LocalDateTime from = LocalDateTime.now().minusDays(d);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("days", d);
        try {
            Long total = opsMapper.countUploads(null);
            Long recent = opsMapper.countUploads(from);
            Long sizeAll = opsMapper.sumUploadSize(null);
            Long sizeRecent = opsMapper.sumUploadSize(from);
            m.put("fileTotal", nvl(total));
            m.put("fileRecent", nvl(recent));
            m.put("sizeTotalMb", round2(nvl(sizeAll) / 1024.0 / 1024));
            m.put("sizeRecentMb", round2(nvl(sizeRecent) / 1024.0 / 1024));
            m.put("byType", opsMapper.selectUploadStatsByType(from));
            m.put("daily", opsMapper.selectUploadDaily(from));
            m.put("topUploaders", opsMapper.selectTopUploaders(from, 10));
        } catch (Throwable t) {
            m.put("error", t.getMessage());
        }
        m.put("disk", systemMetrics.disk());
        return m;
    }

    // ==================== 业务巡检 ====================

    @Override
    public Map<String, Object> bizInspect(int hours) {
        int h = hours <= 0 ? 24 : Math.min(hours, 24 * 30);
        LocalDateTime from = LocalDateTime.now().minusHours(h);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("hours", h);
        m.put("timestamp", OpsTimeUtil.format(LocalDateTime.now()));
        m.put("provider", Map.of("status", "unconfigured", "detail", "未接入供应商健康、价格和额度数据"));
        try {
            Map<String, Object> ai = new LinkedHashMap<>();
            ai.put("statusStats", opsMapper.selectAiTaskStatusStats(from));
            ai.put("typeStats", opsMapper.selectAiTaskTypeStats(from));
            ai.put("failReasons", opsMapper.selectAiTaskFailReasons(from, 10));
            Long total = opsMapper.countAiTasks(null, from);
            Long failed = opsMapper.countAiTasks(3, from);
            Long pending = opsMapper.countAiTasks(0, null);
            Long processing = opsMapper.countAiTasks(1, null);
            ai.put("total", nvl(total));
            ai.put("failed", nvl(failed));
            ai.put("failRate", nvl(total) == 0 ? 0 : round2(nvl(failed) * 100.0 / nvl(total)));
            ai.put("pending", nvl(pending));
            ai.put("processing", nvl(processing));
            Long completed = opsMapper.countAiTasks(2, from);
            ai.put("completed", completed);
            long terminal = nvl(completed) + nvl(failed);
            ai.put("successRate", completed == null || failed == null || terminal == 0 ? null : round2(completed * 100.0 / terminal));
            ai.put("successRateBasis", "已完成 / (已完成 + 已失败)，按创建时间窗口");
            ai.put("latency", opsMapper.selectAiTaskLatency(from));
            m.put("aiTask", ai);

            Map<String, Object> order = new LinkedHashMap<>();
            order.put("stats", opsMapper.selectOrderStats(from));
            order.put("paid", nvl(opsMapper.countPaidOrders(from)));
            m.put("order", order);

            Map<String, Object> queue = new LinkedHashMap<>();
            queue.put("pendingWorks", nvl(opsMapper.countPendingWorks()));
            queue.put("pendingRefunds", nvl(opsMapper.countPendingRefunds()));
            queue.put("pendingReports", nvl(opsMapper.countPendingReports()));
            m.put("queue", queue);

            m.put("userDaily", opsMapper.selectUserDaily(from));
            m.put("orderDaily", opsMapper.selectOrderDaily(from));
            m.put("newUsers", nvl(opsMapper.countNewUsers(from)));

            // 风险提示
            List<Map<String, Object>> risks = new ArrayList<>();
            if (nvl(pending) > 50) risks.add(risk("AI 任务积压", "warning", "待处理任务 " + nvl(pending) + " 个"));
            if (nvl(total) > 0 && nvl(failed) * 100.0 / nvl(total) > 20) {
                risks.add(risk("AI 任务失败率偏高", "danger",
                        "失败率 " + round2(nvl(failed) * 100.0 / nvl(total)) + "%"));
            }
            long pendingRefunds = nvl(opsMapper.countPendingRefunds());
            if (pendingRefunds > 0) risks.add(risk("退款待审核", "warning", pendingRefunds + " 笔待处理"));
            long pendingReports = nvl(opsMapper.countPendingReports());
            if (pendingReports > 0) risks.add(risk("举报待处理", "warning", pendingReports + " 条待处理"));
            long pendingWorks = nvl(opsMapper.countPendingWorks());
            if (pendingWorks > 20) risks.add(risk("作品待审核积压", "info", pendingWorks + " 个待审核"));
            m.put("risks", risks);
        } catch (Throwable t) {
            m.put("error", t.getMessage());
        }
        return m;
    }

    private Map<String, Object> risk(String title, String level, String detail) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", title);
        m.put("level", level);
        m.put("detail", detail);
        return m;
    }

    // ==================== 审计 ====================

    @Override
    public Map<String, Object> adminLogs(String module, String action, String keyword,
                                         String from, String to, int page, int pageSize) {
        int p = OpsTimeUtil.normalizePage(page);
        int size = OpsTimeUtil.normalizeSize(pageSize, 20, 200);
        LocalDateTime f = OpsTimeUtil.parse(from);
        LocalDateTime t = OpsTimeUtil.parse(to);
        Long total = opsMapper.countAdminLogs(emptyToNull(module), emptyToNull(action), emptyToNull(keyword), f, t);
        List<Map<String, Object>> list = opsMapper.selectAdminLogs(emptyToNull(module), emptyToNull(action),
                emptyToNull(keyword), f, t, (p - 1) * size, size);
        return OpsTimeUtil.page(list, p, size, nvl(total));
    }

    @Override
    public Map<String, Object> contentAuditLogs(String checkResult, String contentType, String keyword,
                                                String from, String to, int page, int pageSize) {
        int p = OpsTimeUtil.normalizePage(page);
        int size = OpsTimeUtil.normalizeSize(pageSize, 20, 200);
        LocalDateTime f = OpsTimeUtil.parse(from);
        LocalDateTime t = OpsTimeUtil.parse(to);
        Long total = opsMapper.countContentAuditLogs(emptyToNull(checkResult), emptyToNull(contentType),
                emptyToNull(keyword), f, t);
        List<Map<String, Object>> list = opsMapper.selectContentAuditLogs(emptyToNull(checkResult),
                emptyToNull(contentType), emptyToNull(keyword), f, t, (p - 1) * size, size);
        return OpsTimeUtil.page(list, p, size, nvl(total));
    }

    @Override
    public Map<String, Object> auditStat(int days) {
        int d = days <= 0 ? 7 : Math.min(days, 90);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("days", d);
        try {
            m.put("summary", opsMapper.selectAuditStat(LocalDateTime.now().minusDays(d)));
        } catch (Throwable t) {
            m.put("summary", Collections.emptyMap());
        }
        return m;
    }

    // ==================== 运维配置 ====================

    @Override
    public List<SystemConfig> listOpsConfigs() {
        List<SystemConfig> list = opsMapper.selectOpsConfigs();
        if (list == null) return Collections.emptyList();
        List<SystemConfig> visible = new ArrayList<>();
        for (SystemConfig c : list) {
            if (c.getConfigKey().startsWith(OpsConfigHistory.PREFIX)) continue;
            SystemConfig copy = new SystemConfig();
            org.springframework.beans.BeanUtils.copyProperties(c, copy);
            if (OpsConfigHistory.sensitive(c.getConfigKey()) && c.getConfigValue() != null) copy.setConfigValue(OpsConfigHistory.MASK);
            visible.add(copy);
        }
        return visible;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOpsConfig(String key, String value, String description, String operator) {
        if (key == null || key.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "配置键不能为空");
        }
        String k = key.trim();
        if (!k.startsWith("ops.")) k = "ops." + k;
        configHistory.save(k, value, description, operator);
        log.info("[OPS] 配置更新 {} by {}", k, operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOpsConfig(String key) {
        if (key == null || !key.startsWith("ops.")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "仅允许删除 ops.* 配置");
        }
        configHistory.delete(key);
    }

    // ==================== 工具 ====================

    private static long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private static long toLong(Object o) {
        return o instanceof Number ? ((Number) o).longValue() : 0L;
    }

    private static double round2(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return 0;
        return Math.round(v * 100.0) / 100.0;
    }

    private static String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
