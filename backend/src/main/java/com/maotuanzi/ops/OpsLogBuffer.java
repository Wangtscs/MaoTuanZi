package com.maotuanzi.ops;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 进程内日志环形缓冲（运维日志中心数据源）
 */
@Component
public class OpsLogBuffer {

    /** 缓冲容量 */
    private static final int CAPACITY = 3000;

    private final ConcurrentLinkedDeque<Entry> buffer = new ConcurrentLinkedDeque<>();
    private final ConcurrentHashMap<String, AtomicLong> levelCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> loggerErrorCounts = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong();

    public void append(long timestamp, String level, String logger, String thread,
                       String traceId, String message, String stackTrace) {
        Entry e = new Entry();
        e.id = seq.incrementAndGet();
        e.timestamp = timestamp;
        e.level = level;
        e.logger = logger;
        e.thread = thread;
        e.traceId = traceId;
        e.message = message == null ? "" : (message.length() > 4000 ? message.substring(0, 4000) + "..." : message);
        e.stackTrace = stackTrace;
        buffer.addFirst(e);
        while (buffer.size() > CAPACITY) buffer.pollLast();

        levelCounts.computeIfAbsent(level, k -> new AtomicLong()).incrementAndGet();
        if ("ERROR".equals(level) || "WARN".equals(level)) {
            loggerErrorCounts.computeIfAbsent(logger, k -> new AtomicLong()).incrementAndGet();
        }
    }

    /** 条件查询 */
    public List<Map<String, Object>> query(String level, String keyword, String logger,
                                           String traceId, Long sinceId, int limit) {
        String kw = keyword == null ? null : keyword.toLowerCase(Locale.ROOT);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Entry e : buffer) {
            if (list.size() >= limit) break;
            if (sinceId != null && e.id <= sinceId) break;
            if (level != null && !level.isEmpty() && !level.equalsIgnoreCase(e.level)) continue;
            if (logger != null && !logger.isEmpty() && (e.logger == null || !e.logger.contains(logger))) continue;
            if (traceId != null && !traceId.isEmpty() && !traceId.equals(e.traceId)) continue;
            if (kw != null && !kw.isEmpty()) {
                String hay = (e.message + " " + (e.stackTrace == null ? "" : e.stackTrace)).toLowerCase(Locale.ROOT);
                if (!hay.contains(kw)) continue;
            }
            list.add(e.toMap());
        }
        return list;
    }

    /** 级别分布 + 高频报错来源 */
    public Map<String, Object> stat() {
        Map<String, Object> m = new LinkedHashMap<>();
        Map<String, Long> levels = new LinkedHashMap<>();
        for (String lv : new String[]{"ERROR", "WARN", "INFO", "DEBUG", "TRACE"}) {
            AtomicLong c = levelCounts.get(lv);
            levels.put(lv, c == null ? 0L : c.get());
        }
        m.put("levels", levels);
        m.put("buffered", buffer.size());
        m.put("capacity", CAPACITY);

        List<Map<String, Object>> top = new ArrayList<>();
        loggerErrorCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(10)
                .forEach(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("logger", e.getKey());
                    item.put("count", e.getValue().get());
                    top.add(item);
                });
        m.put("topErrorLoggers", top);

        // 最近 60 分钟按分钟聚合的错误数
        long nowMin = System.currentTimeMillis() / 60000L;
        long[] errPerMin = new long[60];
        long[] warnPerMin = new long[60];
        for (Entry e : buffer) {
            long min = e.timestamp / 60000L;
            int idx = (int) (nowMin - min);
            if (idx < 0 || idx >= 60) continue;
            if ("ERROR".equals(e.level)) errPerMin[59 - idx]++;
            else if ("WARN".equals(e.level)) warnPerMin[59 - idx]++;
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("time", (nowMin - 59 + i) * 60000L);
            item.put("error", errPerMin[i]);
            item.put("warn", warnPerMin[i]);
            trend.add(item);
        }
        m.put("trend", trend);
        return m;
    }

    /** 最近 N 分钟错误日志条数 */
    public long countRecent(String level, int minutes) {
        long from = System.currentTimeMillis() - minutes * 60_000L;
        long c = 0;
        for (Entry e : buffer) {
            if (e.timestamp < from) break;
            if (level == null || level.equalsIgnoreCase(e.level)) c++;
        }
        return c;
    }

    public long latestId() {
        Entry e = buffer.peekFirst();
        return e == null ? 0 : e.id;
    }

    public void clear() {
        buffer.clear();
        levelCounts.clear();
        loggerErrorCounts.clear();
    }

    private static class Entry {
        long id;
        long timestamp;
        String level;
        String logger;
        String thread;
        String traceId;
        String message;
        String stackTrace;

        Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", id);
            m.put("timestamp", timestamp);
            m.put("level", level);
            m.put("logger", logger);
            m.put("thread", thread);
            m.put("traceId", traceId);
            m.put("message", message);
            m.put("stackTrace", stackTrace);
            return m;
        }
    }
}
