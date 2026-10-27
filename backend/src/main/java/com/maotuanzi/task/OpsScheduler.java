package com.maotuanzi.task;

import com.maotuanzi.entity.OpsInstance;
import com.maotuanzi.mapper.OpsMapper;
import com.maotuanzi.ops.OpsHttpMetrics;
import com.maotuanzi.ops.OpsInstanceInfo;
import com.maotuanzi.ops.OpsSystemMetrics;
import com.maotuanzi.service.OpsAlarmService;
import com.maotuanzi.service.OpsJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 运维平台后台调度：实例心跳、任务分发、告警巡检
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpsScheduler {

    private final OpsMapper opsMapper;
    private final OpsInstanceInfo instanceInfo;
    private final OpsSystemMetrics systemMetrics;
    private final OpsHttpMetrics httpMetrics;
    private final OpsJobService jobService;
    private final OpsAlarmService alarmService;

    @Value("${ops.scheduler.enabled:true}")
    private boolean enabled;

    /** 实例心跳上报（15 秒） */
    @Scheduled(initialDelay = 10_000, fixedDelay = 15_000)
    public void heartbeat() {
        if (!enabled) return;
        try {
            Map<String, Object> jvm = systemMetrics.jvm();
            Map<String, Object> os = systemMetrics.os();

            OpsInstance i = new OpsInstance();
            i.setInstanceId(instanceInfo.getInstanceId());
            i.setAppName(instanceInfo.getAppName());
            i.setProfile(instanceInfo.getProfile());
            i.setHost(instanceInfo.getHost());
            i.setIp(instanceInfo.getIp());
            i.setPort(instanceInfo.getPort());
            i.setPid(instanceInfo.getPid());
            i.setVersion(instanceInfo.getVersion());
            i.setStatus("UP");
            i.setCpuLoad(toDouble(os.get("processCpuLoad")));
            i.setSystemCpuLoad(toDouble(os.get("systemCpuLoad")));
            i.setHeapUsedMb(toLong(jvm.get("heapUsedMb")));
            i.setHeapMaxMb(toLong(jvm.get("heapMaxMb")));
            i.setHeapUsedPct(toDouble(jvm.get("heapUsedPct")));
            i.setThreadCount(toInt(jvm.get("threadCount")));
            i.setQps(httpMetrics.qps(60));
            i.setErrorRate(httpMetrics.errorRate(300));
            i.setStartedAt(instanceInfo.getStartedAt());
            i.setLastHeartbeatAt(LocalDateTime.now());
            opsMapper.upsertInstance(i);
        } catch (Throwable t) {
            log.debug("[OPS] 心跳上报失败: {}", t.getMessage());
        }
    }

    /** 任务调度分发（5 秒扫描一次） */
    @Scheduled(initialDelay = 20_000, fixedDelay = 5_000)
    public void dispatchJobs() {
        if (!enabled) return;
        try {
            jobService.dispatch();
        } catch (Throwable t) {
            log.debug("[OPS] 任务分发异常: {}", t.getMessage());
        }
    }

    /** 告警规则巡检（60 秒） */
    @Scheduled(initialDelay = 30_000, fixedDelay = 60_000)
    public void evaluateAlarms() {
        if (!enabled) return;
        try {
            int fired = alarmService.evaluate();
            if (fired > 0) log.info("[OPS] 本轮巡检新增告警 {} 条", fired);
        } catch (Throwable t) {
            log.debug("[OPS] 告警巡检异常: {}", t.getMessage());
        }
    }

    /** 清理离线实例（每 10 分钟） */
    @Scheduled(initialDelay = 300_000, fixedDelay = 600_000)
    public void cleanStaleInstances() {
        if (!enabled) return;
        try {
            opsMapper.deleteStaleInstances(LocalDateTime.now().minusHours(6), instanceInfo.getInstanceId());
        } catch (Throwable t) {
            log.debug("[OPS] 离线实例清理异常: {}", t.getMessage());
        }
    }

    private static Double toDouble(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : null;
    }

    private static Long toLong(Object o) {
        return o instanceof Number ? ((Number) o).longValue() : null;
    }

    private static Integer toInt(Object o) {
        return o instanceof Number ? ((Number) o).intValue() : null;
    }
}
