package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.dto.AiTaskCreateRequest;
import com.maotuanzi.entity.AiStyle;
import com.maotuanzi.entity.AiTask;
import com.maotuanzi.entity.AiTemplate;
import com.maotuanzi.entity.Work;
import com.maotuanzi.mapper.AiStyleMapper;
import com.maotuanzi.mapper.AiTemplateMapper;
import com.maotuanzi.service.AiService;
import com.maotuanzi.service.WorkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "AI 创作")
@RestController
@RequestMapping("/v1/ai")
@RequireAuth
@RequiredArgsConstructor
public class AiController extends BaseController {

    private final AiService aiService;
    private final WorkService workService;
    private final AiStyleMapper styleMapper;
    private final AiTemplateMapper templateMapper;

    @ApiOperation("风格列表")
    @GetMapping("/styles")
    public ApiResult<Object> styles(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return ApiResult.ok(styleMapper.selectByCategory(category));
        }
        Map<String, Object> r = new HashMap<>();
        r.put("portrait_2d", styleMapper.selectByCategory("portrait_2d"));
        r.put("portrait_3d", styleMapper.selectByCategory("portrait_3d"));
        r.put("emoji", styleMapper.selectByCategory("emoji"));
        r.put("card", styleMapper.selectByCategory("card"));
        return ApiResult.ok(r);
    }

    @ApiOperation("模板列表")
    @GetMapping("/templates")
    public ApiResult<Object> templates(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return ApiResult.ok(templateMapper.selectByCategory(category));
        }
        Map<String, Object> r = new HashMap<>();
        r.put("card", templateMapper.selectByCategory("card"));
        r.put("emoji", templateMapper.selectByCategory("emoji"));
        return ApiResult.ok(r);
    }

    @ApiOperation("创建任务")
    @PostMapping("/tasks")
    public ApiResult<AiTask> create(@Valid @RequestBody AiTaskCreateRequest req) {
        return ApiResult.ok(aiService.createTask(req));
    }

    @ApiOperation("查询任务")
    @GetMapping("/tasks/{id}")
    public ApiResult<AiTask> task(@PathVariable Long id) {
        return ApiResult.ok(aiService.queryTask(id));
    }

    @ApiOperation("取消任务")
    @PostMapping("/tasks/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable Long id) {
        aiService.cancelTask(id);
        return ApiResult.ok();
    }

    @ApiOperation("作品作品详情")
    @GetMapping("/works/{id}")
    public ApiResult<Work> work(@PathVariable Long id) {
        return ApiResult.ok(aiService.getWork(id));
    }

    @ApiOperation("作品查询结果")
    @GetMapping("/works/{id}/result")
    public ApiResult<Object> workResult(@PathVariable Long id) {
        return ApiResult.ok(aiService.queryResult(id));
    }
}