package com.maotuanzi.controller.ops;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.service.OpsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 实时监控
 */
@Api(tags = "运维平台-实时监控")
@RestController
@RequestMapping("/ops/monitor")
@RequireAdmin(permission = "ops.metrics")
@RequiredArgsConstructor
public class OpsMonitorController extends BaseController {

    private final OpsService opsService;

    @ApiOperation("实时指标快照")
    @GetMapping("/realtime")
    public ApiResult<Map<String, Object>> realtime() {
        return ApiResult.ok(opsService.realtime());
    }

    @ApiOperation("指标字典")
    @GetMapping("/catalog")
    public ApiResult<List<Map<String, Object>>> catalog() {
        return ApiResult.ok(opsService.metricCatalog());
    }

    @ApiOperation("请求时间线（秒级）")
    @GetMapping("/timeline")
    public ApiResult<Map<String, Object>> timeline(@RequestParam(defaultValue = "120") int seconds) {
        return ApiResult.ok(opsService.httpTimeline(seconds));
    }

    @ApiOperation("接口 TOP 排名")
    @GetMapping("/endpoints")
    public ApiResult<Map<String, Object>> endpoints(@RequestParam(defaultValue = "count") String orderBy,
                                                    @RequestParam(defaultValue = "20") int limit) {
        return ApiResult.ok(opsService.httpEndpoints(orderBy, limit));
    }

    @ApiOperation("指标历史趋势")
    @GetMapping("/series")
    public ApiResult<Map<String, Object>> series(@RequestParam(required = false) String keys,
                                                 @RequestParam(defaultValue = "60") int minutes) {
        List<String> keyList = (keys == null || keys.trim().isEmpty())
                ? null : Arrays.asList(keys.split(","));
        return ApiResult.ok(opsService.metricSeries(keyList, minutes));
    }

    @ApiOperation("重置进程内 HTTP 指标")
    @DeleteMapping("/metrics")
    public ApiResult<Void> reset() {
        opsService.resetHttpMetrics();
        return ApiResult.ok();
    }
}
