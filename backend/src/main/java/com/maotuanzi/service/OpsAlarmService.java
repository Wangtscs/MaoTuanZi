package com.maotuanzi.service;

import com.maotuanzi.entity.OpsAlarmRule;

import java.util.List;
import java.util.Map;

/**
 * 运维告警服务
 */
public interface OpsAlarmService {

    // ==================== 规则 ====================

    List<OpsAlarmRule> listRules(String keyword, Integer enabled);

    OpsAlarmRule getRule(Long id);

    Long createRule(OpsAlarmRule rule, String operator);

    void updateRule(OpsAlarmRule rule);

    void deleteRule(Long id);

    void toggleRule(Long id, Integer enabled);

    /** 用当前指标值试算规则命中情况 */
    Map<String, Object> previewRule(OpsAlarmRule rule);

    // ==================== 记录 ====================

    Map<String, Object> listRecords(String status, String level, String keyword,
                                    String from, String to, int page, int pageSize);

    void ack(Long id, String remark, String operator);

    void resolve(Long id);

    Map<String, Object> stat(int days);

    // ==================== 引擎 ====================

    /** 执行一轮规则巡检，返回本轮新增告警数 */
    int evaluate();
}
