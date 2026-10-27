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
 * 系统资源
 */
@Api(tags = "运维平台-系统资源")
@RestController
@RequestMapping("/ops/system")
@RequireAdmin(permission = "ops.system")
@RequiredArgsConstructor
public class OpsSystemController extends BaseController {

    private final OpsService opsService;

    @ApiOperation("系统 / JVM / 磁盘 / 连接池")
    @GetMapping("/resource")
    public ApiResult<Map<String, Object>> resource() {
        return ApiResult.ok(opsService.systemResource());
    }

    @ApiOperation("线程栈快照")
    @GetMapping("/threads")
    public ApiResult<Map<String, Object>> threads(@RequestParam(required = false) String state,
                                                  @RequestParam(defaultValue = "100") int limit) {
        return ApiResult.ok(opsService.threadDump(state, limit));
    }
}
