package com.maotuanzi.controller.ops;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.OpsAlarmRule;
import com.maotuanzi.security.AdminContext;
import com.maotuanzi.service.OpsAlarmService;
import com.maotuanzi.service.OpsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 告警中心
 */
@Api(tags = "运维平台-告警中心")
@RestController
@RequestMapping("/ops/alarm")
@RequireAdmin(permission = "ops.alarm")
@RequiredArgsConstructor
public class OpsAlarmController extends BaseController {

    private final OpsAlarmService alarmService;
    private final OpsService opsService;

    @ApiOperation("告警指标字典（不包含实时采样值）")
    @GetMapping("/metric-catalog")
    @RequireAdmin(permission = "ops.alarm")
    public ApiResult<List<Map<String, Object>>> metricCatalog() {
        return ApiResult.ok(opsService.metricCatalog());
    }

    // ==================== 规则 ====================

    @ApiOperation("告警规则列表")
    @GetMapping("/rules")
    public ApiResult<List<OpsAlarmRule>> rules(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Integer enabled) {
        return ApiResult.ok(alarmService.listRules(keyword, enabled));
    }

    @ApiOperation("告警规则详情")
    @GetMapping("/rules/{id}")
    public ApiResult<OpsAlarmRule> rule(@PathVariable Long id) {
        return ApiResult.ok(alarmService.getRule(id));
    }

    @ApiOperation("新增告警规则")
    @PostMapping("/rules")
    public ApiResult<Map<String, Object>> createRule(@RequestBody OpsAlarmRule rule) {
        Long id = alarmService.createRule(rule, operator());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        return ApiResult.ok(m);
    }

    @ApiOperation("修改告警规则")
    @PutMapping("/rules/{id}")
    public ApiResult<Void> updateRule(@PathVariable Long id, @RequestBody OpsAlarmRule rule) {
        rule.setId(id);
        alarmService.updateRule(rule);
        return ApiResult.ok();
    }

    @ApiOperation("删除告警规则")
    @DeleteMapping("/rules/{id}")
    public ApiResult<Void> deleteRule(@PathVariable Long id) {
        alarmService.deleteRule(id);
        return ApiResult.ok();
    }

    @ApiOperation("启用 / 停用规则")
    @PutMapping("/rules/{id}/toggle")
    public ApiResult<Void> toggleRule(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        alarmService.toggleRule(id, body.get("enabled"));
        return ApiResult.ok();
    }

    @ApiOperation("规则试算")
    @PostMapping("/rules/preview")
    public ApiResult<Map<String, Object>> preview(@RequestBody OpsAlarmRule rule) {
        return ApiResult.ok(alarmService.previewRule(rule));
    }

    // ==================== 记录 ====================

    @ApiOperation("告警记录列表")
    @GetMapping("/records")
    public ApiResult<Map<String, Object>> records(@RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String level,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String from,
                                                  @RequestParam(required = false) String to,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(alarmService.listRecords(status, level, keyword, from, to, page, pageSize));
    }

    @ApiOperation("认领告警")
    @PutMapping("/records/{id}/ack")
    public ApiResult<Void> ack(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        alarmService.ack(id, body == null ? null : body.get("remark"), operator());
        return ApiResult.ok();
    }

    @ApiOperation("手动恢复告警")
    @PutMapping("/records/{id}/resolve")
    public ApiResult<Void> resolve(@PathVariable Long id) {
        alarmService.resolve(id);
        return ApiResult.ok();
    }

    @ApiOperation("告警统计")
    @GetMapping("/stat")
    public ApiResult<Map<String, Object>> stat(@RequestParam(defaultValue = "7") int days) {
        return ApiResult.ok(alarmService.stat(days));
    }

    @ApiOperation("立即执行一轮巡检")
    @PostMapping("/evaluate")
    public ApiResult<Map<String, Object>> evaluate() {
        int fired = alarmService.evaluate();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("fired", fired);
        return ApiResult.ok(m);
    }

    private String operator() {
        Long id = AdminContext.getAdminId();
        return id == null ? "unknown" : ("admin#" + id);
    }
}
