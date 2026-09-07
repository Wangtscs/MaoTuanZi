package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.dto.Model3DCreateRequest;
import com.maotuanzi.dto.Model3DTaskResult;
import com.maotuanzi.service.Model3DService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 3D 模型生成（阶段3）。
 */
@Api(tags = "3D 模型生成")
@RestController
@RequestMapping("/v1/ai/3d/tasks")
@RequireAuth
@RequiredArgsConstructor
public class Model3DController extends BaseController {

    private final Model3DService model3DService;

    @ApiOperation("创建 3D 生成任务")
    @PostMapping
    public ApiResult<Model3DTaskResult> create(@Valid @RequestBody Model3DCreateRequest req) {
        return ApiResult.ok(model3DService.create(req.getSourceImageUrl(), req.getStyle()));
    }

    @ApiOperation("查询 3D 生成任务")
    @GetMapping("/{id}")
    public ApiResult<Model3DTaskResult> get(@PathVariable String id) {
        return ApiResult.ok(model3DService.get(id));
    }
}
