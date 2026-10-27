package com.maotuanzi.controller.ops;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.service.OpsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 运维总览
 */
@Api(tags = "运维平台-总览")
@RestController
@RequestMapping("/ops")
@RequireAdmin
@RequiredArgsConstructor
public class OpsOverviewController extends BaseController {

    private final OpsService opsService;

    @ApiOperation("运维总览")
    @GetMapping("/overview")
    @RequireAdmin(permission = "ops.view")
    public ApiResult<Map<String, Object>> overview() {
        return ApiResult.ok(opsService.overview());
    }

    @ApiOperation("健康检查明细")
    @GetMapping("/health")
    @RequireAdmin(permission = "ops.view")
    public ApiResult<Map<String, Object>> health() {
        return ApiResult.ok(opsService.health());
    }

    @ApiOperation("服务实例列表")
    @GetMapping("/instances")
    @RequireAdmin(permission = "ops.view")
    public ApiResult<List<Map<String, Object>>> instances() {
        return ApiResult.ok(opsService.instances());
    }
}
