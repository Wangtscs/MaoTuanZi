package com.maotuanzi.ops;

import com.maotuanzi.mapper.OpsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 指标注册中心：把各来源指标统一成 metricKey → value，
 * 供实时监控、指标快照落库、告警规则评估三处共用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpsMetricRegistry {

    private final OpsHttpMetrics httpMetrics;
    private final OpsSystemMetrics systemMetrics;
    private final OpsLogBuffer logBuffer;
    private final OpsMapper opsMapper;

    /** 业务类指标缓存（30 秒），避免高频查库 */
    private volatile long bizCacheAt = 0L;
    private volatile Map<String, Double> bizCache = new LinkedHashMap<>();

    /** 需要落库做趋势的指标 */
    public static final List<String> SNAPSHOT_KEYS = Arrays.asList(
            "jvm.heap.used.pct", "jvm.thread.count", "jvm.gc.time.ms",
            "system.cpu.load", "system.mem.used.pct", "system.disk.free.pct",
            "http.qps", "http.error.rate.5m", "http.p95.5m", "http.avg.5m",
            "db.pool.used.pct", "redis.available", "redis.used.memory.mb",
            "ai.task.pending.count", "ai.task.fail.rate.1h", "log.error.count.5m"
    );

    /** 指标元信息：key → [显示名, 单位] */
    public static final Map<String, String[]> METRIC_META = new LinkedHashMap<>();

    static {
        METRIC_META.put("jvm.heap.used.pct", new String[]{"JVM 堆内存使用率", "%"});
        METRIC_META.put("jvm.heap.used.mb", new String[]{"JVM 堆已用", "MB"});
        METRIC_META.put("jvm.nonheap.used.mb", new String[]{"JVM 非堆已用", "MB"});
        METRIC_META.put("jvm.thread.count", new String[]{"线程数", "个"});
        METRIC_META.put("jvm.gc.count", new String[]{"GC 次数", "次"});
        METRIC_META.put("jvm.gc.time.ms", new String[]{"GC 累计耗时", "ms"});
        METRIC_META.put("system.cpu.load", new String[]{"进程 CPU 负载", "ratio"});
        METRIC_META.put("system.cpu.total.load", new String[]{"系统 CPU 负载", "ratio"});
        METRIC_META.put("system.load.average", new String[]{"系统平均负载", ""});
        METRIC_META.put("system.mem.used.pct", new String[]{"物理内存使用率", "%"});
        METRIC_META.put("system.disk.free.pct", new String[]{"磁盘剩余空间比", "%"});
        METRIC_META.put("http.qps", new String[]{"QPS(60s)", "req/s"});
        METRIC_META.put("http.qps.10s", new String[]{"QPS(10s)", "req/s"});
        METRIC_META.put("http.count.5m", new String[]{"请求数(5m)", "个"});
        METRIC_META.put("http.error.rate.5m", new String[]{"错误率(5m)", "%"});
        METRIC_META.put("http.avg.5m", new String[]{"平均延迟(5m)", "ms"});
        METRIC_META.put("http.p95.5m", new String[]{"P95 延迟(5m)", "ms"});
        METRIC_META.put("http.p99.5m", new String[]{"P99 延迟(5m)", "ms"});
        METRIC_META.put("db.pool.used.pct", new String[]{"连接池使用率", "%"});
        METRIC_META.put("db.pool.active", new String[]{"活跃连接数", "个"});
        METRIC_META.put("db.pool.idle", new String[]{"空闲连接数", "个"});
        METRIC_META.put("db.pool.waiting", new String[]{"等待连接线程", "个"});
        METRIC_META.put("redis.available", new String[]{"Redis 可用性", "0/1"});
        METRIC_META.put("redis.used.memory.mb", new String[]{"Redis 内存", "MB"});
        METRIC_META.put("redis.hit.rate", new String[]{"Redis 命中率", "%"});
        METRIC_META.put("redis.ops", new String[]{"Redis QPS", "op/s"});
        METRIC_META.put("ai.task.pending.count", new String[]{"AI 待处理任务", "个"});
        METRIC_META.put("ai.task.processing.count", new String[]{"AI 生成中任务", "个"});
        METRIC_META.put("ai.task.fail.rate.1h", new String[]{"AI 任务失败率(1h)", "%"});
        METRIC_META.put("log.error.count.5m", new String[]{"ERROR 日志数(5m)", "条"});
        METRIC_META.put("log.warn.count.5m", new String[]{"WARN 日志数(5m)", "条"});
    }

    /**
     * 采集当前全部指标值
     */
    public Map<String, Double> collect() {
        Map<String, Double> m = new LinkedHashMap<>();
        sample(() -> {
            Map<String, Object> jvm = systemMetrics.jvm();
            m.put("jvm.heap.used.pct", toDouble(jvm.get("heapUsedPct")));
            m.put("jvm.heap.used.mb", toDouble(jvm.get("heapUsedMb")));
            m.put("jvm.nonheap.used.mb", toDouble(jvm.get("nonHeapUsedMb")));
            m.put("jvm.thread.count", toDouble(jvm.get("threadCount")));
            m.put("jvm.gc.count", toDouble(jvm.get("gcCount")));
            m.put("jvm.gc.time.ms", toDouble(jvm.get("gcTimeMs")));

        });
        sample(() -> {
            Map<String, Object> os = systemMetrics.os();
            Double procCpu = toDouble(os.get("processCpuLoad"));
            m.put("system.cpu.load", procCpu == null || procCpu < 0 ? null : procCpu);
            m.put("system.cpu.total.load", toDouble(os.get("systemCpuLoad")));
            m.put("system.load.average", toDouble(os.get("loadAverage")));
            m.put("system.mem.used.pct", toDouble(os.get("memUsedPct")));

        });
        sample(() -> {
            Map<String, Object> disk = systemMetrics.disk();
            m.put("system.disk.free.pct", toDouble(disk.get("freePct")));
        });
        sample(() -> {
            m.put("http.qps", httpMetrics.qps(60));
            m.put("http.qps.10s", httpMetrics.qps(10));
            OpsHttpMetrics.Window w5 = httpMetrics.window(300);
            m.put("http.count.5m", (double) w5.count);
            m.put("http.error.rate.5m", w5.count == 0 ? 0 : round2(w5.errors * 100.0 / w5.count));
            m.put("http.avg.5m", w5.count == 0 ? 0 : round2((double) w5.sumMs / w5.count));
            m.put("http.p95.5m", httpMetrics.percentile(5, 0.95));
            m.put("http.p99.5m", httpMetrics.percentile(5, 0.99));

        });
        sample(() -> {
            Map<String, Object> pool = systemMetrics.dbPool();
            m.put("db.pool.used.pct", toDouble(pool.get("usedPct")));
            m.put("db.pool.active", toDouble(pool.get("activeConnections")));
            m.put("db.pool.idle", toDouble(pool.get("idleConnections")));
            m.put("db.pool.waiting", toDouble(pool.get("threadsAwaitingConnection")));

        });
        sample(() -> {
            Map<String, Object> redis = systemMetrics.redis();
            m.put("redis.available", redis.get("ok") instanceof Boolean ? Boolean.TRUE.equals(redis.get("ok")) ? 1.0 : 0.0 : null);
            m.put("redis.used.memory.mb", toDouble(redis.get("usedMemoryMb")));
            m.put("redis.hit.rate", toDouble(redis.get("hitRate")));
            m.put("redis.ops", toDouble(redis.get("instantaneousOpsPerSec")));

        });
        sample(() -> {
            m.put("log.error.count.5m", (double) logBuffer.countRecent("ERROR", 5));
            m.put("log.warn.count.5m", (double) logBuffer.countRecent("WARN", 5));

        });
        m.putAll(biz());
        METRIC_META.keySet().forEach(k -> m.putIfAbsent(k, null));
        m.replaceAll((k, v) -> v != null && Double.isFinite(v) && v >= 0 ? v : null);
        return m;
    }

    private void sample(Runnable collector) {
        try { collector.run(); }
        catch (Exception e) { log.debug("[OPS] 指标来源不可用: {}", e.getClass().getSimpleName()); }
    }

    /** 业务指标（带 30s 缓存） */
    private Map<String, Double> biz() {
        long now = System.currentTimeMillis();
        if (now - bizCacheAt < 30_000L && !bizCache.isEmpty()) {
            return bizCache;
        }
        Map<String, Double> m = new LinkedHashMap<>();
        try {
            LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
            Long pending = opsMapper.countAiTasks(0, null);
            Long processing = opsMapper.countAiTasks(1, null);
            Long total1h = opsMapper.countAiTasks(null, hourAgo);
            Long failed1h = opsMapper.countAiTasks(3, hourAgo);
            m.put("ai.task.pending.count", pending == null ? null : pending.doubleValue());
            m.put("ai.task.processing.count", processing == null ? null : processing.doubleValue());
            m.put("ai.task.fail.rate.1h", total1h == null || total1h == 0 || failed1h == null ? null
                    : round2(failed1h * 100.0 / total1h));
        } catch (Throwable t) {
            log.debug("[OPS] 业务指标采集降级: {}", t.getMessage());
            m.put("ai.task.pending.count", null);
            m.put("ai.task.processing.count", null);
            m.put("ai.task.fail.rate.1h", null);
        }
        bizCache = m;
        bizCacheAt = now;
        return m;
    }

    /** 指标字典（供前端下拉选择） */
    public List<Map<String, Object>> catalog() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, String[]> e : METRIC_META.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", e.getKey());
            item.put("name", e.getValue()[0]);
            item.put("unit", e.getValue()[1]);
            item.put("snapshot", SNAPSHOT_KEYS.contains(e.getKey()));
            list.add(item);
        }
        return list;
    }

    public static String nameOf(String key) {
        String[] meta = METRIC_META.get(key);
        return meta == null ? key : meta[0];
    }

    public static String unitOf(String key) {
        String[] meta = METRIC_META.get(key);
        return meta == null ? "" : meta[1];
    }

    private static long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private static Double toDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        if (o instanceof Boolean) return ((Boolean) o) ? 1.0 : 0.0;
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    private static double round2(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return 0;
        return Math.round(v * 100.0) / 100.0;
    }
}
