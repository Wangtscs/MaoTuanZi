package com.maotuanzi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.entity.OpsJob;
import com.maotuanzi.entity.OpsJobRecord;
import com.maotuanzi.entity.OpsMetricSnapshot;
import com.maotuanzi.mapper.OpsMapper;
import com.maotuanzi.ops.OpsInstanceInfo;
import com.maotuanzi.ops.OpsMetricRegistry;
import com.maotuanzi.ops.OpsSystemMetrics;
import com.maotuanzi.ops.OpsTimeUtil;
import com.maotuanzi.service.OpsAlarmService;
import com.maotuanzi.service.OpsJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 运维任务调度服务实现
 *
 * <p>自带轻量调度器：Cron 解析 + 到点触发 + 执行记录，无需引入 XXL-JOB / Quartz。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpsJobServiceImpl implements OpsJobService {

    /** 内置处理器：code → [名称, 说明] */
    private static final Map<String, String[]> HANDLERS = new LinkedHashMap<>();

    static {
        HANDLERS.put("metricsSnapshot", new String[]{"指标快照采集", "采集当前指标写入 ops_metric_snapshots，供趋势图使用"});
        HANDLERS.put("dbPing", new String[]{"数据库连通性检查", "执行 SELECT 1 并记录延迟"});
        HANDLERS.put("redisPing", new String[]{"Redis 连通性检查", "PING + INFO 检查"});
        HANDLERS.put("diskCheck", new String[]{"磁盘空间检查", "检查各分区剩余空间，低于阈值时告警日志"});
        HANDLERS.put("aiTaskTimeout", new String[]{"AI 任务超时扫描", "将超时未完成的 AI 任务置为失败，params: timeoutMinutes/limit"});
        HANDLERS.put("cleanupIdempotency", new String[]{"清理过期幂等键", "删除 idempotency_keys 中已过期记录"});
        HANDLERS.put("cleanupSnapshots", new String[]{"清理历史指标快照", "params: keepDays（默认 7）"});
        HANDLERS.put("cleanupJobRecords", new String[]{"清理任务执行记录", "params: keepDays（默认 30）"});
        HANDLERS.put("cleanupUploads", new String[]{"清理孤儿上传记录", "params: keepDays（默认 30）"});
        HANDLERS.put("cleanupInstances", new String[]{"清理离线实例", "params: keepMinutes（默认 60）"});
        HANDLERS.put("alarmEvaluate", new String[]{"告警规则巡检", "执行一轮阈值评估"});
    }

    private final OpsMapper opsMapper;
    private final OpsMetricRegistry metricRegistry;
    private final OpsSystemMetrics systemMetrics;
    private final OpsInstanceInfo instanceInfo;
    private final OpsAlarmService alarmService;
    private final ObjectMapper objectMapper;

    /** 正在执行的任务，避免同一任务并发 */
    private final Map<Long, Long> running = new ConcurrentHashMap<>();

    private ExecutorService executor;

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(4, new ThreadFactory() {
            private final AtomicInteger idx = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "ops-job-" + idx.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
    }

    @PreDestroy
    public void destroy() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    // ==================== CRUD ====================

    @Override
    public List<OpsJob> listJobs(String keyword, Integer enabled) {
        List<OpsJob> list = opsMapper.selectJobs(emptyToNull(keyword), enabled);
        if (list == null) return Collections.emptyList();
        for (OpsJob job : list) {
            if (job.getNextRunAt() == null && job.getEnabled() != null && job.getEnabled() == 1) {
                job.setNextRunAt(nextRun(job.getCron(), LocalDateTime.now()));
            }
        }
        return list;
    }

    @Override
    public OpsJob getJob(Long id) {
        OpsJob job = opsMapper.selectJob(id);
        if (job == null) throw new BizException(ErrorCode.NOT_FOUND, "运维任务不存在");
        return job;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createJob(OpsJob job) {
        validate(job);
        if (opsMapper.selectJobByCode(job.getJobCode()) != null) {
            throw new BizException(ErrorCode.STATE_CONFLICT, "任务编码已存在");
        }
        if (job.getEnabled() == null) job.setEnabled(1);
        if (job.getJobType() == null || job.getJobType().isEmpty()) job.setJobType("builtin");
        job.setNextRunAt(nextRun(job.getCron(), LocalDateTime.now()));
        opsMapper.insertJob(job);
        return job.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJob(OpsJob job) {
        getJob(job.getId());
        validate(job);
        job.setNextRunAt(nextRun(job.getCron(), LocalDateTime.now()));
        opsMapper.updateJob(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Long id) {
        OpsJob job = getJob(id);
        if ("builtin".equals(job.getJobType())) {
            throw new BizException(ErrorCode.STATE_CONFLICT, "内置任务不可删除，可先停用");
        }
        opsMapper.deleteJob(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleJob(Long id, Integer enabled) {
        OpsJob job = getJob(id);
        job.setEnabled(enabled == null ? 0 : enabled);
        job.setNextRunAt(job.getEnabled() == 1 ? nextRun(job.getCron(), LocalDateTime.now()) : null);
        opsMapper.updateJob(job);
    }

    private void validate(OpsJob job) {
        if (job.getJobCode() == null || job.getJobCode().trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "任务编码不能为空");
        }
        if (job.getJobName() == null || job.getJobName().trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "任务名称不能为空");
        }
        if (job.getHandler() == null || !HANDLERS.containsKey(job.getHandler())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "处理器不存在，可选：" + HANDLERS.keySet());
        }
        if (nextRun(job.getCron(), LocalDateTime.now()) == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "Cron 表达式非法（需 6 位：秒 分 时 日 月 周）");
        }
    }

    // ==================== 执行 ====================

    @Override
    public Map<String, Object> trigger(Long id, String operator) {
        OpsJob job = getJob(id);
        return execute(job, "MANUAL", operator);
    }

    @Override
    public void dispatch() {
        List<OpsJob> jobs;
        try {
            jobs = opsMapper.selectJobs(null, 1);
        } catch (Throwable t) {
            log.debug("[OPS] 任务调度加载失败: {}", t.getMessage());
            return;
        }
        if (jobs == null || jobs.isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();
        for (OpsJob job : jobs) {
            try {
                if (job.getNextRunAt() == null) {
                    LocalDateTime next = nextRun(job.getCron(), now);
                    if (next != null) opsMapper.updateJobNextRun(job.getId(), next);
                    continue;
                }
                if (job.getNextRunAt().isAfter(now)) continue;
                if (running.containsKey(job.getId())) continue;

                LocalDateTime next = nextRun(job.getCron(), now);
                opsMapper.updateJobNextRun(job.getId(), next);
                executor.submit(() -> execute(job, "AUTO", "scheduler"));
            } catch (Throwable t) {
                log.warn("[OPS] 任务[{}]调度异常: {}", job.getJobCode(), t.getMessage());
            }
        }
    }

    /** 执行一次任务并落库记录 */
    private Map<String, Object> execute(OpsJob job, String triggerType, String operator) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jobCode", job.getJobCode());
        result.put("triggerType", triggerType);

        if (running.putIfAbsent(job.getId(), System.currentTimeMillis()) != null) {
            result.put("status", "SKIPPED");
            result.put("message", "任务正在执行中，本次跳过");
            return result;
        }

        LocalDateTime start = LocalDateTime.now();
        long startMs = System.currentTimeMillis();
        OpsJobRecord record = new OpsJobRecord();
        record.setJobId(job.getId());
        record.setJobCode(job.getJobCode());
        record.setStatus("RUNNING");
        record.setTriggerType(triggerType);
        record.setInstanceId(instanceInfo.getInstanceId());
        record.setStartAt(start);
        try {
            opsMapper.insertJobRecord(record);
            opsMapper.updateJobRunning(job.getId(), start);
        } catch (Throwable t) {
            log.debug("[OPS] 任务记录写入失败: {}", t.getMessage());
        }

        String status = "SUCCESS";
        String message;
        try {
            message = runHandler(job);
            if (message == null) message = "执行完成";
        } catch (Throwable t) {
            status = "FAILED";
            message = t.getClass().getSimpleName() + ": " + t.getMessage();
            log.warn("[OPS] 任务[{}]执行失败", job.getJobCode(), t);
        } finally {
            running.remove(job.getId());
        }

        long cost = System.currentTimeMillis() - startMs;
        if (message.length() > 2000) message = message.substring(0, 2000);
        try {
            if (record.getId() != null) {
                opsMapper.finishJobRecord(record.getId(), status, LocalDateTime.now(), cost, message);
            }
            opsMapper.updateJobRunResult(job.getId(), status, start,
                    nextRun(job.getCron(), LocalDateTime.now()), cost, message);
        } catch (Throwable t) {
            log.debug("[OPS] 任务结果回写失败: {}", t.getMessage());
        }

        result.put("status", status);
        result.put("message", message);
        result.put("durationMs", cost);
        result.put("operator", operator);
        return result;
    }

    /** 内置处理器分发 */
    private String runHandler(OpsJob job) throws Exception {
        Map<String, Object> params = parseParams(job.getParams());
        String handler = job.getHandler() == null ? "" : job.getHandler();
        switch (handler) {
            case "metricsSnapshot":
                return doMetricsSnapshot();
            case "dbPing":
                return doDbPing();
            case "redisPing":
                return doRedisPing();
            case "diskCheck":
                return doDiskCheck(params);
            case "aiTaskTimeout":
                return doAiTaskTimeout(params);
            case "cleanupIdempotency":
                return "清理过期幂等键 " + opsMapper.deleteIdempotencyBefore(LocalDateTime.now()) + " 条";
            case "cleanupSnapshots": {
                int keep = intParam(params, "keepDays", 7);
                int n = opsMapper.deleteSnapshotsBefore(LocalDateTime.now().minusDays(keep));
                return "清理 " + keep + " 天前指标快照 " + n + " 条";
            }
            case "cleanupJobRecords": {
                int keep = intParam(params, "keepDays", 30);
                int n = opsMapper.deleteJobRecordsBefore(LocalDateTime.now().minusDays(keep));
                return "清理 " + keep + " 天前任务记录 " + n + " 条";
            }
            case "cleanupUploads": {
                int keep = intParam(params, "keepDays", 30);
                int n = opsMapper.deleteOrphanUploads(LocalDateTime.now().minusDays(keep));
                return "清理 " + keep + " 天前孤儿上传记录 " + n + " 条";
            }
            case "cleanupInstances": {
                int keep = intParam(params, "keepMinutes", 60);
                int n = opsMapper.deleteStaleInstances(LocalDateTime.now().minusMinutes(keep),
                        instanceInfo.getInstanceId());
                return "清理离线实例 " + n + " 个";
            }
            case "alarmEvaluate": {
                int fired = alarmService.evaluate();
                return "告警巡检完成，新增告警 " + fired + " 条";
            }
            default:
                throw new IllegalStateException("未知处理器: " + handler);
        }
    }

    private String doMetricsSnapshot() {
        Map<String, Double> metrics = metricRegistry.collect();
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        List<OpsMetricSnapshot> list = new ArrayList<>();
        for (String key : OpsMetricRegistry.SNAPSHOT_KEYS) {
            Double v = metrics.get(key);
            if (v == null) continue;
            OpsMetricSnapshot s = new OpsMetricSnapshot();
            s.setInstanceId(instanceInfo.getInstanceId());
            s.setMetricKey(key);
            s.setMetricValue(v);
            s.setSampleTime(now);
            list.add(s);
        }
        if (list.isEmpty()) return "无可用指标";
        opsMapper.batchInsertSnapshots(list);
        return "写入指标快照 " + list.size() + " 条";
    }

    private String doDbPing() {
        Map<String, Object> r = systemMetrics.dbPing();
        boolean ok = Boolean.TRUE.equals(r.get("ok"));
        if (!ok) throw new IllegalStateException("数据库不可用: " + r.get("error"));
        return "数据库正常，延迟 " + r.get("latencyMs") + "ms，版本 " + r.get("productVersion");
    }

    private String doRedisPing() {
        Map<String, Object> r = systemMetrics.redis();
        boolean ok = Boolean.TRUE.equals(r.get("ok"));
        if (!ok) throw new IllegalStateException("Redis 不可用: " + r.get("error"));
        return "Redis 正常，延迟 " + r.get("latencyMs") + "ms，内存 " + r.get("usedMemoryMb") + "MB";
    }

    private String doDiskCheck(Map<String, Object> params) {
        double warn = doubleParam(params, "warnFreePct", 15);
        Map<String, Object> disk = systemMetrics.disk();
        double freePct = toDouble(disk.get("freePct"));
        String msg = "磁盘剩余 " + freePct + "%（" + disk.get("freeGb") + "GB / " + disk.get("totalGb") + "GB）";
        if (freePct < warn) {
            log.warn("[OPS] 磁盘空间不足：{}", msg);
            return msg + "，低于告警线 " + warn + "%";
        }
        return msg;
    }

    private String doAiTaskTimeout(Map<String, Object> params) {
        int timeoutMinutes = intParam(params, "timeoutMinutes", 30);
        int limit = intParam(params, "limit", 200);
        int n = opsMapper.timeoutAiTasks(LocalDateTime.now().minusMinutes(timeoutMinutes), limit);
        return "扫描超时 " + timeoutMinutes + " 分钟以上的 AI 任务，置为失败 " + n + " 条";
    }

    // ==================== 记录与统计 ====================

    @Override
    public Map<String, Object> listRecords(Long jobId, String status, int page, int pageSize) {
        int p = OpsTimeUtil.normalizePage(page);
        int size = OpsTimeUtil.normalizeSize(pageSize, 20, 200);
        Long total = opsMapper.countJobRecords(jobId, emptyToNull(status));
        List<OpsJobRecord> list = opsMapper.selectJobRecords(jobId, emptyToNull(status), (p - 1) * size, size);
        return OpsTimeUtil.page(list, p, size, total == null ? 0 : total);
    }

    @Override
    public Map<String, Object> stat(int days) {
        int d = days <= 0 ? 7 : Math.min(days, 90);
        Map<String, Object> stat = opsMapper.selectJobStat(null);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("summary", stat == null ? new LinkedHashMap<String, Object>() : stat);
        Long recent = opsMapper.countJobRecords(null, null);
        Long failed = opsMapper.countJobRecords(null, "FAILED");
        m.put("recordTotal", recent == null ? 0 : recent);
        m.put("recordFailed", failed == null ? 0 : failed);
        m.put("running", running.size());
        m.put("days", d);
        return m;
    }

    @Override
    public List<Map<String, Object>> handlers() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, String[]> e : HANDLERS.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("handler", e.getKey());
            m.put("name", e.getValue()[0]);
            m.put("description", e.getValue()[1]);
            list.add(m);
        }
        return list;
    }

    // ==================== 工具 ====================

    /** 解析 6 位 / 5 位 Cron，返回下一次执行时间 */
    public static LocalDateTime nextRun(String cron, LocalDateTime base) {
        if (cron == null || cron.trim().isEmpty()) return null;
        String expr = cron.trim();
        try {
            String[] parts = expr.split("\\s+");
            if (parts.length == 5) {
                expr = "0 " + expr;
            } else if (parts.length != 6) {
                return null;
            }
            if (!CronExpression.isValidExpression(expr)) return null;
            return CronExpression.parse(expr).next(base);
        } catch (Throwable t) {
            return null;
        }
    }

    private Map<String, Object> parseParams(String json) {
        if (json == null || json.trim().isEmpty()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private static int intParam(Map<String, Object> params, String key, int def) {
        Object v = params.get(key);
        if (v == null) return def;
        try {
            return v instanceof Number ? ((Number) v).intValue() : Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return def;
        }
    }

    private static double doubleParam(Map<String, Object> params, String key, double def) {
        Object v = params.get(key);
        if (v == null) return def;
        try {
            return v instanceof Number ? ((Number) v).doubleValue() : Double.parseDouble(String.valueOf(v));
        } catch (Exception e) {
            return def;
        }
    }

    private static double toDouble(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : 0;
    }

    private static String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    /** 正在执行的任务 ID 集合（供总览展示） */
    public List<Long> runningJobIds() {
        return new ArrayList<>(running.keySet());
    }

    /** 内置处理器 code 列表 */
    public static List<String> handlerCodes() {
        return new ArrayList<>(HANDLERS.keySet());
    }
}
