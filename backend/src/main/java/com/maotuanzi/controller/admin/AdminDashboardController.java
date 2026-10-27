package com.maotuanzi.controller.admin;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "管理后台-仪表盘")
@RestController
@RequestMapping("/admin/dashboard")
@RequireAdmin
@RequiredArgsConstructor
public class AdminDashboardController extends BaseController {

    private final AdminService adminService;

    @ApiOperation("核心指标")
    @GetMapping
    public ApiResult<Map<String, Object>> dashboard() {
        return ApiResult.ok(adminService.dashboard());
    }
}