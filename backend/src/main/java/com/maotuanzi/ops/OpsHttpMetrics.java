package com.maotuanzi.ops;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * HTTP 调用指标采集（进程内滑动窗口，无外部依赖）
 *
 * <p>秒级桶用于 QPS / 错误率，分钟级直方图用于 P95 / P99。</p>
 */
@Component
public class OpsHttpMetrics {

    /** 秒级桶保留时长 */
    private static final int KEEP_SECONDS = 3600;
    /** 分钟级直方图保留时长 */
    private static final int KEEP_MINUTES = 180;
    /** 慢请求样本上限 */
    private static final int SLOW_SAMPLE_LIMIT = 200;
    /** 慢请求阈值（毫秒） */
    private static final long SLOW_THRESHOLD_MS = 1000L;

    /** 延迟直方图上界（毫秒） */
    private static final long[] LATENCY_BOUNDS = {
            5, 10, 20, 50, 100, 200, 300, 500, 800, 1200, 2000, 3000, 5000, 8000, Long.MAX_VALUE
    };

    private final ConcurrentSkipListMap<Long, SecBucket> secBuckets = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<Long, AtomicLongArray> minHistograms = new ConcurrentSkipListMap<>();
    private final ConcurrentHashMap<String, EndpointStat> endpoints = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, AtomicLong> statusCounts = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<Map<String, Object>> slowRequests = new ConcurrentLinkedDeque<>();

    private final AtomicLong totalCount = new AtomicLong();
    private final AtomicLong totalErrors = new AtomicLong();
    private final AtomicLong totalCostMs = new AtomicLong();
    private final long startedAtMs = System.currentTimeMillis();

    // ==================== 采集 ====================

    public void record(String method, String pattern, String uri, int status, long costMs, String errorType) {
        long now = System.currentTimeMillis();
        long sec = now / 1000L;
        long min = now / 60000L;
        boolean error = status >= 500 || errorType != null;

        SecBucket sb = secBuckets.computeIfAbsent(sec, k -> new SecBucket());
        sb.count.incrementAndGet();
        sb.sumMs.addAndGet(costMs);
        sb.maxMs.accumulateAndGet(costMs, Math::max);
        if (error) sb.errors.incrementAndGet();
        if (status >= 400 && status < 500) sb.clientErrors.incrementAndGet();

        AtomicLongArray hist = minHistograms.computeIfAbsent(min, k -> new AtomicLongArray(LATENCY_BOUNDS.length));
        hist.incrementAndGet(indexOf(costMs));

        String key = (method == null ? "-" : method) + " " + (pattern == null || pattern.isEmpty() ? uri : pattern);
        EndpointStat es = endpoints.computeIfAbsent(key, k -> new EndpointStat(k));
        es.count.incrementAndGet();
        es.sumMs.addAndGet(costMs);
        es.maxMs.accumulateAndGet(costMs, Math::max);
        if (error) es.errors.incrementAndGet();
        es.lastAccessMs.set(now);

        statusCounts.computeIfAbsent(status, k -> new AtomicLong()).incrementAndGet();

        totalCount.incrementAndGet();
        totalCostMs.addAndGet(costMs);
        if (error) totalErrors.incrementAndGet();

        if (costMs >= SLOW_THRESHOLD_MS || error) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("time", now);
            item.put("method", method);
            item.put("uri", uri);
            item.put("status", status);
            item.put("costMs", costMs);
            item.put("errorType", errorType);
            slowRequests.addFirst(item);
            while (slowRequests.size() > SLOW_SAMPLE_LIMIT) slowRequests.pollLast();
        }

        evict(sec, min);
    }

    private void evict(long nowSec, long nowMin) {
        Long first;
        while ((first = secBuckets.isEmpty() ? null : secBuckets.firstKey()) != null
                && first < nowSec - KEEP_SECONDS) {
            secBuckets.remove(first);
        }
        while ((first = minHistograms.isEmpty() ? null : minHistograms.firstKey()) != null
                && first < nowMin - KEEP_MINUTES) {
            minHistograms.remove(first);
        }
        if (endpoints.size() > 500) {
            long expire = System.currentTimeMillis() - 6 * 3600_000L;
            endpoints.entrySet().removeIf(e -> e.getValue().lastAccessMs.get() < expire);
        }
    }

    private static int indexOf(long costMs) {
        for (int i = 0; i < LATENCY_BOUNDS.length; i++) {
            if (costMs <= LATENCY_BOUNDS[i]) return i;
        }
        return LATENCY_BOUNDS.length - 1;
    }

    // ==================== 查询 ====================

    /** 最近 N 秒窗口聚合 */
    public Window window(int seconds) {
        long nowSec = System.currentTimeMillis() / 1000L;
        long from = nowSec - seconds;
        Window w = new Window();
        w.seconds = seconds;
        for (Map.Entry<Long, SecBucket> e : secBuckets.tailMap(from, true).entrySet()) {
            if (e.getKey() > nowSec) continue;
            SecBucket b = e.getValue();
            w.count += b.count.get();
            w.errors += b.errors.get();
            w.clientErrors += b.clientErrors.get();
            w.sumMs += b.sumMs.get();
            w.maxMs = Math.max(w.maxMs, b.maxMs.get());
        }
        return w;
    }

    /** 最近 N 秒 QPS */
    public double qps(int seconds) {
        Window w = window(seconds);
        return seconds <= 0 ? 0 : round2((double) w.count / seconds);
    }

    /** 最近 N 秒错误率（%） */
    public double errorRate(int seconds) {
        Window w = window(seconds);
        return w.count == 0 ? 0 : round2(w.errors * 100.0 / w.count);
    }

    /** 最近 N 秒平均延迟（毫秒） */
    public double avgLatency(int seconds) {
        Window w = window(seconds);
        return w.count == 0 ? 0 : round2((double) w.sumMs / w.count);
    }

    /** 最近 N 分钟分位延迟（毫秒，直方图上界近似） */
    public double percentile(int minutes, double p) {
        long nowMin = System.currentTimeMillis() / 60000L;
        long from = nowMin - minutes;
        long[] merged = new long[LATENCY_BOUNDS.length];
        long total = 0;
        for (Map.Entry<Long, AtomicLongArray> e : minHistograms.tailMap(from, true).entrySet()) {
            AtomicLongArray arr = e.getValue();
            for (int i = 0; i < merged.length; i++) {
                long v = arr.get(i);
                merged[i] += v;
                total += v;
            }
        }
        if (total == 0) return 0;
        long target = (long) Math.ceil(total * p);
        long acc = 0;
        for (int i = 0; i < merged.length; i++) {
            acc += merged[i];
            if (acc >= target) {
                long bound = LATENCY_BOUNDS[i];
                return bound == Long.MAX_VALUE ? 10000 : bound;
            }
        }
        return 0;
    }

    /** 秒级时间线（用于实时曲线） */
    public List<Map<String, Object>> timeline(int seconds) {
        long nowSec = System.currentTimeMillis() / 1000L;
        List<Map<String, Object>> list = new ArrayList<>(seconds);
        for (long s = nowSec - seconds + 1; s <= nowSec; s++) {
            SecBucket b = secBuckets.get(s);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("time", s * 1000L);
            long c = b == null ? 0 : b.count.get();
            long er = b == null ? 0 : b.errors.get();
            m.put("count", c);
            m.put("errors", er);
            m.put("avgMs", c == 0 ? 0 : round2((double) b.sumMs.get() / c));
            m.put("maxMs", b == null ? 0 : b.maxMs.get());
            list.add(m);
        }
        return list;
    }

    /** 接口维度 TOP 排名 */
    public List<Map<String, Object>> topEndpoints(String orderBy, int limit) {
        List<EndpointStat> all = new ArrayList<>(endpoints.values());
        Comparator<EndpointStat> cmp;
        if ("avg".equalsIgnoreCase(orderBy)) {
            cmp = Comparator.comparingDouble(EndpointStat::avg).reversed();
        } else if ("error".equalsIgnoreCase(orderBy)) {
            cmp = Comparator.comparingLong((EndpointStat s) -> s.errors.get()).reversed();
        } else if ("max".equalsIgnoreCase(orderBy)) {
            cmp = Comparator.comparingLong((EndpointStat s) -> s.maxMs.get()).reversed();
        } else {
            cmp = Comparator.comparingLong((EndpointStat s) -> s.count.get()).reversed();
        }
        all.sort(cmp);
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, all.size()); i++) {
            EndpointStat s = all.get(i);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("endpoint", s.key);
            m.put("count", s.count.get());
            m.put("errors", s.errors.get());
            m.put("errorRate", s.count.get() == 0 ? 0 : round2(s.errors.get() * 100.0 / s.count.get()));
            m.put("avgMs", round2(s.avg()));
            m.put("maxMs", s.maxMs.get());
            m.put("lastAccessAt", s.lastAccessMs.get());
            list.add(m);
        }
        return list;
    }

    /** 状态码分布 */
    public Map<String, Long> statusDistribution() {
        Map<String, Long> m = new LinkedHashMap<>();
        List<Integer> keys = new ArrayList<>(statusCounts.keySet());
        keys.sort(Comparator.naturalOrder());
        for (Integer k : keys) m.put(String.valueOf(k), statusCounts.get(k).get());
        return m;
    }

    /** 慢请求 / 异常请求样本 */
    public List<Map<String, Object>> slowSamples(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> m : slowRequests) {
            if (list.size() >= limit) break;
            list.add(m);
        }
        return list;
    }

    public Map<String, Object> summary() {
        Map<String, Object> m = new LinkedHashMap<>();
        long total = totalCount.get();
        m.put("totalRequests", total);
        m.put("totalErrors", totalErrors.get());
        m.put("errorRateAll", total == 0 ? 0 : round2(totalErrors.get() * 100.0 / total));
        m.put("avgMsAll", total == 0 ? 0 : round2((double) totalCostMs.get() / total));
        m.put("collectSinceMs", startedAtMs);
        m.put("endpointCount", endpoints.size());
        return m;
    }

    public void reset() {
        secBuckets.clear();
        minHistograms.clear();
        endpoints.clear();
        statusCounts.clear();
        slowRequests.clear();
        totalCount.set(0);
        totalErrors.set(0);
        totalCostMs.set(0);
    }

    private static double round2(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return 0;
        return Math.round(v * 100.0) / 100.0;
    }

    // ==================== 内部结构 ====================

    private static class SecBucket {
        final AtomicLong count = new AtomicLong();
        final AtomicLong errors = new AtomicLong();
        final AtomicLong clientErrors = new AtomicLong();
        final AtomicLong sumMs = new AtomicLong();
        final AtomicLong maxMs = new AtomicLong();
    }

    private static class EndpointStat {
        final String key;
        final AtomicLong count = new AtomicLong();
        final AtomicLong errors = new AtomicLong();
        final AtomicLong sumMs = new AtomicLong();
        final AtomicLong maxMs = new AtomicLong();
        final AtomicLong lastAccessMs = new AtomicLong();

        EndpointStat(String key) {
            this.key = key;
        }

        double avg() {
            long c = count.get();
            return c == 0 ? 0 : (double) sumMs.get() / c;
        }
    }

    /** 窗口聚合结果 */
    public static class Window {
        public int seconds;
        public long count;
        public long errors;
        public long clientErrors;
        public long sumMs;
        public long maxMs;

        public Map<String, Object> toMap() {
            Map<String, Object> m = new HashMap<>();
            m.put("seconds", seconds);
            m.put("count", count);
            m.put("errors", errors);
            m.put("clientErrors", clientErrors);
            m.put("qps", seconds == 0 ? 0 : round2((double) count / seconds));
            m.put("errorRate", count == 0 ? 0 : round2(errors * 100.0 / count));
            m.put("avgMs", count == 0 ? 0 : round2((double) sumMs / count));
            m.put("maxMs", maxMs);
            return m;
        }
    }
}
