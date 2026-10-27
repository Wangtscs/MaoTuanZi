package com.maotuanzi.mapper;

import com.maotuanzi.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 运维平台数据访问
 */
@Mapper
public interface OpsMapper {

    // ==================== 实例 ====================
    int upsertInstance(OpsInstance instance);

    List<OpsInstance> selectInstances();

    int deleteStaleInstances(@Param("before") LocalDateTime before, @Param("exclude") String exclude);

    // ==================== 指标快照 ====================
    int batchInsertSnapshots(@Param("list") List<OpsMetricSnapshot> list);

    List<Map<String, Object>> selectSnapshots(@Param("keys") List<String> keys,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);

    int deleteSnapshotsBefore(@Param("before") LocalDateTime before);

    // ==================== 告警规则 ====================
    List<OpsAlarmRule> selectAlarmRules(@Param("keyword") String keyword, @Param("enabled") Integer enabled);

    OpsAlarmRule selectAlarmRule(@Param("id") Long id);

    int insertAlarmRule(OpsAlarmRule rule);

    int updateAlarmRule(OpsAlarmRule rule);

    int deleteAlarmRule(@Param("id") Long id);

    int updateAlarmRuleCheck(@Param("id") Long id, @Param("value") Double value,
                             @Param("firing") Integer firing, @Param("time") LocalDateTime time);

    // ==================== 告警记录 ====================
    List<OpsAlarmRecord> selectAlarmRecords(@Param("status") String status,
                                            @Param("level") String level,
                                            @Param("keyword") String keyword,
                                            @Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);

    Long countAlarmRecords(@Param("status") String status,
                           @Param("level") String level,
                           @Param("keyword") String keyword,
                           @Param("from") LocalDateTime from,
                           @Param("to") LocalDateTime to);

    OpsAlarmRecord selectFiringByRule(@Param("ruleId") Long ruleId);

    int insertAlarmRecord(OpsAlarmRecord record);

    int resolveAlarmRecord(@Param("id") Long id, @Param("resolvedAt") LocalDateTime resolvedAt,
                           @Param("durationSeconds") int durationSeconds, @Param("value") Double value);

    int ackAlarmRecord(@Param("id") Long id, @Param("ackBy") String ackBy,
                       @Param("remark") String remark, @Param("time") LocalDateTime time);

    Map<String, Object> selectAlarmStat(@Param("from") LocalDateTime from);

    List<Map<String, Object>> selectAlarmTrend(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // ==================== 任务 ====================
    List<OpsJob> selectJobs(@Param("keyword") String keyword, @Param("enabled") Integer enabled);

    OpsJob selectJob(@Param("id") Long id);

    OpsJob selectJobByCode(@Param("jobCode") String jobCode);

    int insertJob(OpsJob job);

    int updateJob(OpsJob job);

    int deleteJob(@Param("id") Long id);

    int updateJobRunResult(@Param("id") Long id, @Param("status") String status,
                           @Param("lastRunAt") LocalDateTime lastRunAt,
                           @Param("nextRunAt") LocalDateTime nextRunAt,
                           @Param("durationMs") Long durationMs,
                           @Param("message") String message);

    int updateJobNextRun(@Param("id") Long id, @Param("nextRunAt") LocalDateTime nextRunAt);

    int updateJobRunning(@Param("id") Long id, @Param("lastRunAt") LocalDateTime lastRunAt);

    Map<String, Object> selectJobStat(@Param("from") LocalDateTime from);

    // ==================== 任务执行记录 ====================
    int insertJobRecord(OpsJobRecord record);

    int finishJobRecord(@Param("id") Long id, @Param("status") String status,
                        @Param("endAt") LocalDateTime endAt,
                        @Param("durationMs") Long durationMs,
                        @Param("message") String message);

    List<OpsJobRecord> selectJobRecords(@Param("jobId") Long jobId, @Param("status") String status,
                                        @Param("offset") int offset, @Param("limit") int limit);

    Long countJobRecords(@Param("jobId") Long jobId, @Param("status") String status);

    int deleteJobRecordsBefore(@Param("before") LocalDateTime before);

    // ==================== 业务巡检 ====================
    List<Map<String, Object>> selectAiTaskStatusStats(@Param("from") LocalDateTime from);

    List<Map<String, Object>> selectAiTaskTypeStats(@Param("from") LocalDateTime from);

    List<Map<String, Object>> selectAiTaskFailReasons(@Param("from") LocalDateTime from, @Param("limit") int limit);

    Long countAiTasks(@Param("status") Integer status, @Param("from") LocalDateTime from);

    Map<String, Object> selectAiTaskLatency(@Param("from") LocalDateTime from);

    int timeoutAiTasks(@Param("before") LocalDateTime before, @Param("limit") int limit);

    List<Map<String, Object>> selectOrderStats(@Param("from") LocalDateTime from);

    Long countNewUsers(@Param("from") LocalDateTime from);

    Long countPaidOrders(@Param("from") LocalDateTime from);

    Long countPendingWorks();

    Long countPendingRefunds();

    Long countPendingReports();

    List<Map<String, Object>> selectUserDaily(@Param("from") LocalDateTime from);

    List<Map<String, Object>> selectOrderDaily(@Param("from") LocalDateTime from);

    // ==================== 存储 ====================
    Long countUploads(@Param("from") LocalDateTime from);

    Long sumUploadSize(@Param("from") LocalDateTime from);

    List<Map<String, Object>> selectUploadStatsByType(@Param("from") LocalDateTime from);

    List<Map<String, Object>> selectUploadDaily(@Param("from") LocalDateTime from);

    List<Map<String, Object>> selectTopUploaders(@Param("from") LocalDateTime from, @Param("limit") int limit);

    int deleteOrphanUploads(@Param("before") LocalDateTime before);

    // ==================== 审计 ====================
    List<Map<String, Object>> selectAdminLogs(@Param("module") String module,
                                              @Param("action") String action,
                                              @Param("keyword") String keyword,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to,
                                              @Param("offset") int offset,
                                              @Param("limit") int limit);

    Long countAdminLogs(@Param("module") String module,
                        @Param("action") String action,
                        @Param("keyword") String keyword,
                        @Param("from") LocalDateTime from,
                        @Param("to") LocalDateTime to);

    List<Map<String, Object>> selectContentAuditLogs(@Param("checkResult") String checkResult,
                                                     @Param("contentType") String contentType,
                                                     @Param("keyword") String keyword,
                                                     @Param("from") LocalDateTime from,
                                                     @Param("to") LocalDateTime to,
                                                     @Param("offset") int offset,
                                                     @Param("limit") int limit);

    Long countContentAuditLogs(@Param("checkResult") String checkResult,
                               @Param("contentType") String contentType,
                               @Param("keyword") String keyword,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    Map<String, Object> selectAuditStat(@Param("from") LocalDateTime from);

    // ==================== 配置 ====================
    List<SystemConfig> selectOpsConfigs();

    SystemConfig selectOpsConfigForUpdate(@Param("configKey") String configKey);

    int restoreOpsConfig(@Param("configKey") String configKey, @Param("configValue") String configValue,
                         @Param("description") String description, @Param("updatedBy") String updatedBy);

    int upsertOpsConfig(@Param("configKey") String configKey, @Param("configValue") String configValue,
                        @Param("description") String description, @Param("updatedBy") String updatedBy);

    int deleteOpsConfig(@Param("configKey") String configKey);

    // ==================== 清理 ====================
    int deleteIdempotencyBefore(@Param("before") LocalDateTime before);

    // ==================== 数据库元数据 ====================
    List<Map<String, Object>> selectTableStats(@Param("schema") String schema);
}
