package com.maotuanzi.controller.ops;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.SystemConfig;
import com.maotuanzi.security.AdminContext;
import com.maotuanzi.service.OpsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 运维配置（system_configs 中 ops.* 前缀）
 */
@Api(tags = "运维平台-配置")
@RestController
@RequestMapping("/ops/configs")
@RequireAdmin(permission = "ops.config")
@RequiredArgsConstructor
public class OpsConfigController extends BaseController {

    private final OpsService opsService;

    @ApiOperation("运维配置列表")
    @GetMapping
    public ApiResult<List<SystemConfig>> list() {
        return ApiResult.ok(opsService.listOpsConfigs());
    }

    @ApiOperation("新增 / 更新配置")
    @PostMapping
    public ApiResult<Void> save(@RequestBody Map<String, String> body) {
        Long adminId = AdminContext.getAdminId();
        opsService.saveOpsConfig(body.get("configKey"), body.get("configValue"),
                body.get("description"), adminId == null ? "unknown" : "admin#" + adminId);
        return ApiResult.ok();
    }

    @ApiOperation("删除配置")
    @DeleteMapping
    public ApiResult<Void> delete(@RequestParam String configKey) {
        opsService.deleteOpsConfig(configKey);
        return ApiResult.ok();
    }
}
