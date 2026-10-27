package com.maotuanzi.controller.admin;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.SensitiveWord;
import com.maotuanzi.entity.SquarePost;
import com.maotuanzi.service.AdminService;
import com.maotuanzi.service.AuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理后台-内容")
@RestController
@RequestMapping("/admin/content")
@RequireAdmin
@RequiredArgsConstructor
public class AdminContentController extends BaseController {

    private final AdminService adminService;
    private final AuditService auditService;

    @GetMapping("/posts")
    public ApiResult<PageResult<SquarePost>> list(@RequestParam(required = false) Integer status,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(adminService.listPosts(status, page, pageSize));
    }

    @PostMapping("/posts/{id}/offline")
    @RequireAdmin(permission = "content.post.review")
    public ApiResult<Void> offline(@PathVariable Long id) {
        adminService.offlinePost(id);
        return ApiResult.ok();
    }

    @GetMapping("/sensitive-words")
    public ApiResult<List<SensitiveWord>> words() {
        return ApiResult.ok(adminService.listSensitiveWords());
    }

    @PostMapping("/sensitive-words")
    @RequireAdmin(permission = "content.sensitive.manage")
    public ApiResult<Void> add(@RequestBody SensitiveWord word) {
        adminService.addSensitiveWord(word);
        auditService.reload();
        return ApiResult.ok();
    }

    @DeleteMapping("/sensitive-words/{id}")
    @RequireAdmin(permission = "content.sensitive.manage")
    public ApiResult<Void> delete(@PathVariable Integer id) {
        adminService.deleteSensitiveWord(id);
        auditService.reload();
        return ApiResult.ok();
    }
}