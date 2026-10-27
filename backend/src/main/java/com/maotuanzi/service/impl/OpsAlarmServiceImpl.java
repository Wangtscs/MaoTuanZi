package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.entity.OpsAlarmRecord;
import com.maotuanzi.entity.OpsAlarmRule;
import com.maotuanzi.mapper.OpsMapper;
import com.maotuanzi.ops.OpsMetricRegistry;
import com.maotuanzi.ops.OpsTimeUtil;
import com.maotuanzi.ops.OpsNotificationService;
import com.maotuanzi.service.OpsAlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 运维告警服务实现：规则 CRUD + 阈值巡检引擎
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpsAlarmServiceImpl implements OpsAlarmService {

    private final OpsMapper opsMapper;
    private final OpsMetricRegistry metricRegistry;
    private final OpsNotificationService notifications;

    /** ruleId → 连续命中次数 */
    private final Map<Long, AtomicInteger> hitCounters = new ConcurrentHashMap<>();
    /** ruleId → 上次触发时间（毫秒），用于静默期 */
    private final Map<Long, Long> lastFiredAt = new ConcurrentHashMap<>();
    /** 最近一轮巡检时间 */
    private volatile LocalDateTime lastEvaluateAt;

    // ==================== 规则 ====================

    @Override
    public List<OpsAlarmRule> listRules(String keyword, Integer enabled) {
        return opsMapper.selectAlarmRules(emptyToNull(keyword), enabled);
    }

    @Override
    public OpsAlarmRule getRule(Long id) {
        OpsAlarmRule rule = opsMapper.selectAlarmRule(id);
        if (rule == null) throw new BizException(ErrorCode.NOT_FOUND, "告警规则不存在");
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRule(OpsAlarmRule rule, String operator) {
        validate(rule);
        rule.setCreatedBy(operator);
        if (rule.getEnabled() == null) rule.setEnabled(1);
        if (rule.getFiring() == null) rule.setFiring(0);
        if (rule.getDurationChecks() == null || rule.getDurationChecks() < 1) rule.setDurationChecks(1);
        if (rule.getSilenceMinutes() == null || rule.getSilenceMinutes() < 0) rule.setSilenceMinutes(0);
        if (rule.getLevel() == null || rule.getLevel().isEmpty()) rule.setLevel("P2");
        opsMapper.insertAlarmRule(rule);
        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(OpsAlarmRule rule) {
        getRule(rule.getId());
        validate(rule);
        opsMapper.updateAlarmRule(rule);
        hitCounters.remove(rule.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        getRule(id);
        opsMapper.deleteAlarmRule(id);
        hitCounters.remove(id);
        lastFiredAt.remove(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleRule(Long id, Integer enabled) {
        OpsAlarmRule rule = getRule(id);
        rule.setEnabled(enabled == null ? 0 : enabled);
        opsMapper.updateAlarmRule(rule);
        hitCounters.remove(id);
    }

    @Override
    public Map<String, Object> previewRule(OpsAlarmRule rule) {
        validate(rule);
        Map<String, Double> metrics = metricRegistry.collect();
        Double value = metrics.get(rule.getMetricKey());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("metricKey", rule.getMetricKey());
        m.put("metricName", OpsMetricRegistry.nameOf(rule.getMetricKey()));
        m.put("unit", OpsMetricRegistry.unitOf(rule.getMetricKey()));
        m.put("currentValue", value);
        m.put("sampleStatus", value == null || !Double.isFinite(value) ? "UNKNOWN" : "AVAILABLE");
        m.put("threshold", rule.getThreshold());
        m.put("operator", rule.getOperator());
        m.put("matched", value == null || !Double.isFinite(value) ? null : match(rule.getOperator(), value, rule.getThreshold()));
        m.put("expression", expression(rule));
        return m;
    }

    private void validate(OpsAlarmRule rule) {
        if (rule == null) throw new BizException(ErrorCode.PARAM_INVALID, "参数为空");
        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "规则名称不能为空");
        }
        if (rule.getMetricKey() == null || rule.getMetricKey().trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "监控指标不能为空");
        }
        if (rule.getThreshold() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "阈值不能为空");
        }
        String op = rule.getOperator();
        if (op == null || !("gt".equals(op) || "gte".equals(op) || "lt".equals(op)
                || "lte".equals(op) || "eq".equals(op) || "ne".equals(op))) {
            throw new BizException(ErrorCode.PARAM_INVALID, "比较符仅支持 gt/gte/lt/lte/eq/ne");
        }
    }

    // ==================== 记录 ====================

    @Override
    public Map<String, Object> listRecords(String status, String level, String keyword,
                                           String from, String to, int page, int pageSize) {
        int p = OpsTimeUtil.normalizePage(page);
        int size = OpsTimeUtil.normalizeSize(pageSize, 20, 200);
        LocalDateTime f = OpsTimeUtil.parse(from);
        LocalDateTime t = OpsTimeUtil.parse(to);
        Long total = opsMapper.countAlarmRecords(emptyToNull(status), emptyToNull(level),
                emptyToNull(keyword), f, t);
        List<OpsAlarmRecord> list = opsMapper.selectAlarmRecords(emptyToNull(status), emptyToNull(level),
                emptyToNull(keyword), f, t, (p - 1) * size, size);
        return OpsTimeUtil.page(list, p, size, total == null ? 0 : total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ack(Long id, String remark, String operator) {
        int n = opsMapper.ackAlarmRecord(id, operator, remark, LocalDateTime.now());
        if (n == 0) throw new BizException(ErrorCode.NOT_FOUND, "告警记录不存在");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolve(Long id) {
        int n = opsMapper.resolveAlarmRecord(id, LocalDateTime.now(), 0, null);
        if (n == 0) throw new BizException(ErrorCode.NOT_FOUND, "告警记录不存在或已恢复");
    }

    @Override
    public Map<String, Object> stat(int days) {
        int d = days <= 0 ? 7 : Math.min(days, 90);
        LocalDateTime from = LocalDateTime.now().minusDays(d);
        Map<String, Object> stat = opsMapper.selectAlarmStat(from);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("days", d);
        m.put("summary", stat == null ? new LinkedHashMap<String, Object>() : stat);
        m.put("trend", opsMapper.selectAlarmTrend(from, LocalDateTime.now()));
        List<OpsAlarmRule> rules = opsMapper.selectAlarmRules(null, null);
        int enabled = 0;
        int firing = 0;
        for (OpsAlarmRule r : rules) {
            if (r.getEnabled() != null && r.getEnabled() == 1) enabled++;
            if (r.getFiring() != null && r.getFiring() == 1) firing++;
        }
        m.put("ruleTotal", rules.size());
        m.put("ruleEnabled", enabled);
        m.put("ruleFiring", firing);
        m.put("lastEvaluateAt", OpsTimeUtil.format(lastEvaluateAt));
        return m;
    }

    // ==================== 引擎 ====================

    @Override
    public int evaluate() {
        lastEvaluateAt = LocalDateTime.now();
        List<OpsAlarmRule> rules;
        try {
            rules = opsMapper.selectAlarmRules(null, 1);
        } catch (Throwable t) {
            log.warn("[OPS] 加载告警规则失败: {}", t.getMessage());
            return 0;
        }
        if (rules == null || rules.isEmpty()) return 0;

        Map<String, Double> metrics = metricRegistry.collect();
        int fired = 0;
        LocalDateTime now = LocalDateTime.now();

        for (OpsAlarmRule rule : rules) {
            try {
                Double value = metrics.get(rule.getMetricKey());
                if (value == null || !Double.isFinite(value)) {
                    hitCounters.remove(rule.getId());
                    continue;
                }

                boolean matched = match(rule.getOperator(), value, rule.getThreshold());
                AtomicInteger counter = hitCounters.computeIfAbsent(rule.getId(), k -> new AtomicInteger());
                int need = rule.getDurationChecks() == null || rule.getDurationChecks() < 1
                        ? 1 : rule.getDurationChecks();

                if (matched) {
                    int hits = counter.incrementAndGet();
                    opsMapper.updateAlarmRuleCheck(rule.getId(), value,
                            rule.getFiring() == null ? 0 : rule.getFiring(), now);
                    if (hits < need) continue;

                    OpsAlarmRecord exist = opsMapper.selectFiringByRule(rule.getId());
                    if (exist != null) continue;

                    int silence = rule.getSilenceMinutes() == null ? 0 : rule.getSilenceMinutes();
                    Long last = lastFiredAt.get(rule.getId());
                    if (silence > 0 && last != null
                            && System.currentTimeMillis() - last < silence * 60_000L) {
                        continue;
                    }

                    OpsAlarmRecord record = new OpsAlarmRecord();
                    record.setRuleId(rule.getId());
                    record.setRuleName(rule.getName());
                    record.setMetricKey(rule.getMetricKey());
                    record.setMetricValue(value);
                    record.setThreshold(rule.getThreshold());
                    record.setLevel(rule.getLevel());
                    record.setStatus("FIRING");
                    record.setMessage(String.format("%s 当前值 %s，%s（连续 %d 次）",
                            OpsMetricRegistry.nameOf(rule.getMetricKey()),
                            trimNumber(value), expression(rule), need));
                    record.setFiredAt(now);
                    opsMapper.insertAlarmRecord(record);
                    opsMapper.updateAlarmRuleCheck(rule.getId(), value, 1, now);
                    lastFiredAt.put(rule.getId(), System.currentTimeMillis());
                    fired++;
                    log.warn("[OPS-ALARM] {} 触发：{}", rule.getLevel(), record.getMessage());
                    notifications.notify(rule, record);
                } else {
                    counter.set(0);
                    opsMapper.updateAlarmRuleCheck(rule.getId(), value, 0, now);
                    OpsAlarmRecord exist = opsMapper.selectFiringByRule(rule.getId());
                    if (exist != null) {
                        int duration = exist.getFiredAt() == null ? 0
                                : (int) java.time.Duration.between(exist.getFiredAt(), now).getSeconds();
                        opsMapper.resolveAlarmRecord(exist.getId(), now, duration, value);
                        log.info("[OPS-ALARM] 已恢复：{}（持续 {}s）", rule.getName(), duration);
                    }
                }
            } catch (Throwable t) {
                log.warn("[OPS] 规则[{}]评估异常: {}", rule.getName(), t.getMessage());
            }
        }
        return fired;
    }

    private static boolean match(String op, double value, Double threshold) {
        if (threshold == null) return false;
        switch (op == null ? "gt" : op) {
            case "gt":
                return value > threshold;
            case "gte":
                return value >= threshold;
            case "lt":
                return value < threshold;
            case "lte":
                return value <= threshold;
            case "eq":
                return Math.abs(value - threshold) < 1e-9;
            case "ne":
                return Math.abs(value - threshold) >= 1e-9;
            default:
                return false;
        }
    }

    private static String expression(OpsAlarmRule rule) {
        String symbol;
        switch (rule.getOperator() == null ? "gt" : rule.getOperator()) {
            case "gte":
                symbol = ">=";
                break;
            case "lt":
                symbol = "<";
                break;
            case "lte":
                symbol = "<=";
                break;
            case "eq":
                symbol = "=";
                break;
            case "ne":
                symbol = "!=";
                break;
            default:
                symbol = ">";
        }
        return "阈值 " + symbol + " " + trimNumber(rule.getThreshold())
                + OpsMetricRegistry.unitOf(rule.getMetricKey());
    }

    private static String trimNumber(Double v) {
        if (v == null) return "-";
        if (Math.abs(v - Math.rint(v)) < 1e-9) return String.valueOf((long) v.doubleValue());
        return String.valueOf(Math.round(v * 100.0) / 100.0);
    }

    private static String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    /** 供总览展示：内存中的连续命中情况 */
    public List<Map<String, Object>> hitSnapshot() {
        List<Map<String, Object>> list = new ArrayList<>();
        hitCounters.forEach((k, v) -> {
            if (v.get() <= 0) return;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ruleId", k);
            m.put("hits", v.get());
            list.add(m);
        });
        return list;
    }
}
