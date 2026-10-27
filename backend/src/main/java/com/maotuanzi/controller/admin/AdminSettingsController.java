package com.maotuanzi.controller.admin;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.SystemConfig;
import com.maotuanzi.security.AdminContext;
import com.maotuanzi.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "管理后台-设置")
@RestController
@RequestMapping("/admin/settings")
@RequireAdmin
@RequiredArgsConstructor
public class AdminSettingsController extends BaseController {

    private final AdminService adminService;

    @GetMapping("/configs")
    public ApiResult<List<SystemConfig>> list() {
        return ApiResult.ok(adminService.listConfigs());
    }

    @PutMapping("/configs/{key}")
    @RequireAdmin(permission = "settings.config")
    public ApiResult<Void> update(@PathVariable String key, @RequestBody Map<String, String> body) {
        String adminName = String.valueOf(AdminContext.getAdminId());
        adminService.updateConfig(key, body.get("value"), adminName);
        return ApiResult.ok();
    }
}