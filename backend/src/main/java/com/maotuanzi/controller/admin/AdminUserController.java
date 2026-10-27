package com.maotuanzi.controller.admin;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.User;
import com.maotuanzi.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "管理后台-用户")
@RestController
@RequestMapping("/admin/users")
@RequireAdmin
@RequiredArgsConstructor
public class AdminUserController extends BaseController {

    private final AdminService adminService;

    @GetMapping
    public ApiResult<PageResult<User>> list(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(adminService.listUsers(keyword, status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<User> detail(@PathVariable Long id) {
        return ApiResult.ok(adminService.getUser(id));
    }

    @PostMapping("/{id}/ban")
    @RequireAdmin(permission = "user.ban")
    public ApiResult<Void> ban(@PathVariable Long id) {
        adminService.banUser(id);
        return ApiResult.ok();
    }

    @PostMapping("/{id}/unban")
    @RequireAdmin(permission = "user.ban")
    public ApiResult<Void> unban(@PathVariable Long id) {
        adminService.unbanUser(id);
        return ApiResult.ok();
    }
}