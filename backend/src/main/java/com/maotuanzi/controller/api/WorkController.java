package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.Work;
import com.maotuanzi.service.WorkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "作品")
@RestController
@RequestMapping("/v1/works")
@RequireAuth
@RequiredArgsConstructor
public class WorkController extends BaseController {

    private final WorkService workService;

    @ApiOperation("我的作品")
    @GetMapping
    public ApiResult<PageResult<Work>> list(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(workService.listMine(status, page, pageSize));
    }

    @ApiOperation("详情")
    @GetMapping("/{id}")
    public ApiResult<Work> detail(@PathVariable Long id) {
        return ApiResult.ok(workService.detail(id));
    }

    @ApiOperation("解锁高清")
    @PostMapping("/{id}/unlock")
    public ApiResult<Work> unlock(@PathVariable Long id) {
        return ApiResult.ok(workService.unlockResult(id));
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        workService.delete(id);
        return ApiResult.ok();
    }
}