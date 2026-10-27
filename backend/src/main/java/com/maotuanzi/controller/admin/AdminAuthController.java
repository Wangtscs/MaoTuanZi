package com.maotuanzi.controller.admin;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "管理后台-认证")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController extends BaseController {

    private final AdminService adminService;

    @ApiOperation("管理员登录")
    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpServletRequest req) {
        return ApiResult.ok(adminService.login(body.get("username"), body.get("password"), getClientIp(req)));
    }

    @ApiOperation("登出")
    @PostMapping("/logout")
    @RequireAdmin
    public ApiResult<Void> logout(HttpServletRequest req) {
        String h = req.getHeader("X-Admin-Token");
        if (h == null || !h.startsWith("Bearer ")) h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) adminService.logout(h.substring(7));
        return ApiResult.ok();
    }

    @ApiOperation("当前管理员信息")
    @GetMapping("/me")
    @RequireAdmin
    public ApiResult<Map<String, Object>> me() {
        Map<String, Object> r = new java.util.HashMap<>();
        r.put("adminId", com.maotuanzi.security.AdminContext.getAdminId());
        r.put("perms", com.maotuanzi.security.AdminContext.getPerms());
        return ApiResult.ok(r);
    }

    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) return ip.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}