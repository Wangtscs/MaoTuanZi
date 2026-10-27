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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 业务巡检（AI 任务、订单、待办队列水位）
 */
@Api(tags = "运维平台-业务巡检")
@RestController
@RequestMapping("/ops/biz")
@RequireAdmin(permission = "ops.view")
@RequiredArgsConstructor
public class OpsBizController extends BaseController {

    private final OpsService opsService;

    @ApiOperation("业务巡检")
    @GetMapping("/inspect")
    public ApiResult<Map<String, Object>> inspect(@RequestParam(defaultValue = "24") int hours) {
        return ApiResult.ok(opsService.bizInspect(hours));
    }
}
