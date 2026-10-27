package com.maotuanzi.controller.admin;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.Work;
import com.maotuanzi.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "管理后台-作品")
@RestController
@RequestMapping("/admin/works")
@RequireAdmin
@RequiredArgsConstructor
public class AdminWorkController extends BaseController {

    private final AdminService adminService;

    @GetMapping
    public ApiResult<PageResult<Work>> list(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(adminService.listWorks(status, page, pageSize));
    }

    @PostMapping("/{id}/review")
    @RequireAdmin(permission = "work.review")
    public ApiResult<Void> review(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        adminService.reviewWork(id, body.get("status"));
        return ApiResult.ok();
    }
}