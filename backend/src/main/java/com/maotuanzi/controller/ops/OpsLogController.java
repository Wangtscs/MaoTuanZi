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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 日志中心
 */
@Api(tags = "运维平台-日志中心")
@RestController
@RequestMapping("/ops/logs")
@RequireAdmin(permission = "ops.log")
@RequiredArgsConstructor
public class OpsLogController extends BaseController {

    private final OpsService opsService;

    @ApiOperation("查询运行日志")
    @GetMapping
    public ApiResult<Map<String, Object>> list(@RequestParam(required = false) String level,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String logger,
                                               @RequestParam(required = false) String traceId,
                                               @RequestParam(required = false) Long sinceId,
                                               @RequestParam(defaultValue = "200") int limit) {
        return ApiResult.ok(opsService.queryLogs(level, keyword, logger, traceId, sinceId, limit));
    }

    @ApiOperation("日志统计（级别分布 / 趋势 / 高频来源）")
    @GetMapping("/stat")
    public ApiResult<Map<String, Object>> stat() {
        return ApiResult.ok(opsService.logStat());
    }

    @ApiOperation("清空日志缓冲")
    @DeleteMapping
    public ApiResult<Void> clear() {
        opsService.clearLogs();
        return ApiResult.ok();
    }

    @ApiOperation("调整日志采集级别")
    @PutMapping("/level")
    public ApiResult<Map<String, Object>> setLevel(@RequestBody Map<String, String> body) {
        return ApiResult.ok(opsService.setLogLevel(body.get("level")));
    }
}
