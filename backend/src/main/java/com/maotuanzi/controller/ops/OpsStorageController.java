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
 * 存储与文件
 */
@Api(tags = "运维平台-存储")
@RestController
@RequestMapping("/ops/storage")
@RequireAdmin(permission = "ops.storage")
@RequiredArgsConstructor
public class OpsStorageController extends BaseController {

    private final OpsService opsService;

    @ApiOperation("存储总览")
    @GetMapping("/overview")
    public ApiResult<Map<String, Object>> overview(@RequestParam(defaultValue = "7") int days) {
        return ApiResult.ok(opsService.storageOverview(days));
    }
}
