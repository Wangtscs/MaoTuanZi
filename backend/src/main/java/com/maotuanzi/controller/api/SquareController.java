package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.dto.PostCreateRequest;
import com.maotuanzi.entity.PostComment;
import com.maotuanzi.entity.SquarePost;
import com.maotuanzi.service.SquareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api(tags = "萌宠广场")
@RestController
@RequestMapping("/v1/square")
@RequiredArgsConstructor
public class SquareController extends BaseController {

    private final SquareService squareService;

    @ApiOperation("帖子流（支持 tab: latest/hottest/3d_zone/challenge）")
    @GetMapping("/posts")
    public ApiResult<PageResult<SquarePost>> list(@RequestParam(required = false) String tab,
                                                  @RequestParam(required = false) String topic,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(squareService.list(tab, topic, status, page, pageSize));
    }

    @ApiOperation("帖子详情")
    @GetMapping("/posts/{id}")
    public ApiResult<SquarePost> detail(@PathVariable Long id) {
        return ApiResult.ok(squareService.detail(id));
    }

    @ApiOperation("发布帖子")
    @PostMapping("/posts")
    @RequireAuth
    public ApiResult<SquarePost> publish(@Valid @RequestBody PostCreateRequest req) {
        return ApiResult.ok(squareService.publish(req));
    }

    @ApiOperation("删除帖子")
    @DeleteMapping("/posts/{id}")
    @RequireAuth
    public ApiResult<Void> delete(@PathVariable Long id) {
        squareService.delete(id);
        return ApiResult.ok();
    }

    @ApiOperation("点赞")
    @PostMapping("/posts/{id}/like")
    @RequireAuth
    public ApiResult<Void> like(@PathVariable Long id) {
        squareService.like(id);
        return ApiResult.ok();
    }

    @ApiOperation("取消点赞")
    @DeleteMapping("/posts/{id}/like")
    @RequireAuth
    public ApiResult<Void> unlike(@PathVariable Long id) {
        squareService.unlike(id);
        return ApiResult.ok();
    }

    @ApiOperation("举报")
    @PostMapping("/posts/{id}/report")
    @RequireAuth
    public ApiResult<Void> report(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer type = ((Number) body.getOrDefault("type", 5)).intValue();
        squareService.report(id, type, (String) body.get("reason"));
        return ApiResult.ok();
    }

    @ApiOperation("评论列表")
    @GetMapping("/posts/{id}/comments")
    public ApiResult<List<PostComment>> comments(@PathVariable Long id) {
        return ApiResult.ok(squareService.comments(id));
    }

    @ApiOperation("发表评论")
    @PostMapping("/posts/{id}/comments")
    @RequireAuth
    public ApiResult<Void> addComment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        squareService.addComment(id, body.get("content"));
        return ApiResult.ok();
    }
}