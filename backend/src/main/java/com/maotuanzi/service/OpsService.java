package com.maotuanzi.service;

import com.maotuanzi.entity.SystemConfig;

import java.util.List;
import java.util.Map;

/**
 * 运维平台核心服务：总览、监控、系统资源、日志、数据源、存储、业务巡检、审计、配置
 */
public interface OpsService {

    // ==================== 总览 ====================

    /** 运维总览（健康度 + 核心指标 + 告警 + 任务 + 业务水位） */
    Map<String, Object> overview();

    /** 健康检查明细（DB / Redis / 磁盘 / JVM / 接口） */
    Map<String, Object> health();

    // ==================== 实时监控 ====================

    /** 实时指标快照（全量 metricKey → value + 元信息） */
    Map<String, Object> realtime();

    /** 指标字典 */
    List<Map<String, Object>> metricCatalog();

    /** 秒级请求时间线 */
    Map<String, Object> httpTimeline(int seconds);

    /** 接口维度 TOP */
    Map<String, Object> httpEndpoints(String orderBy, int limit);

    /** 历史趋势（来自指标快照表） */
    Map<String, Object> metricSeries(List<String> keys, int minutes);

    /** 实例列表 */
    List<Map<String, Object>> instances();

    /** 重置进程内 HTTP 指标 */
    void resetHttpMetrics();

    // ==================== 系统资源 ====================

    Map<String, Object> systemResource();

    /** 线程栈快照 */
    Map<String, Object> threadDump(String state, int limit);

    // ==================== 日志中心 ====================

    Map<String, Object> queryLogs(String level, String keyword, String logger, String traceId, Long sinceId, int limit);

    Map<String, Object> logStat();

    void clearLogs();

    /** 调整日志采集级别 */
    Map<String, Object> setLogLevel(String level);

    // ==================== 数据源 ====================

    Map<String, Object> dbStatus();

    List<Map<String, Object>> dbTables();

    Map<String, Object> redisStatus();

    // ==================== 存储 ====================

    Map<String, Object> storageOverview(int days);

    // ==================== 业务巡检 ====================

    Map<String, Object> bizInspect(int hours);

    // ==================== 审计 ====================

    Map<String, Object> adminLogs(String module, String action, String keyword, String from, String to, int page, int pageSize);

    Map<String, Object> contentAuditLogs(String checkResult, String contentType, String keyword, String from, String to, int page, int pageSize);

    Map<String, Object> auditStat(int days);

    // ==================== 运维配置 ====================

    List<SystemConfig> listOpsConfigs();

    void saveOpsConfig(String key, String value, String description, String operator);

    void deleteOpsConfig(String key);
}
