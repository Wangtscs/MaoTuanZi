package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.dto.WxLoginRequest;
import com.maotuanzi.entity.User;
import com.maotuanzi.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "用户")
@RestController
@RequestMapping("/v1/users")
@RequireAuth
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;

    @ApiOperation("当前用户信息")
    @GetMapping("/me")
    public ApiResult<User> me() {
        return ApiResult.ok(userService.me());
    }

    @ApiOperation("更新资料")
    @PatchMapping("/me")
    public ApiResult<User> updateMe(@RequestBody WxLoginRequest patch) {
        User u = new User();
        u.setNickname(patch.getNickname());
        u.setAvatarUrl(patch.getAvatarUrl());
        return ApiResult.ok(userService.updateMe(u));
    }

    @ApiOperation("注销账号")
    @PostMapping("/me/cancel")
    public ApiResult<Void> cancel(@RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? "no_need" : body.getOrDefault("reason", "no_need");
        userService.cancel(reason);
        return ApiResult.ok();
    }

    @ApiOperation("数据导出（GDPR / 个保法）")
    @GetMapping("/me/export")
    public ApiResult<Map<String, Object>> export() {
        Map<String, Object> data = new HashMap<>();
        User u = userService.me();
        data.put("user", u);
        // 实际中聚合所有用户数据并生成 OSS 签名 URL
        data.put("download_url", "https://cdn.maotuanzi.ai/mock/exports/" + u.getId() + ".json");
        data.put("expires_in", 86400);
        return ApiResult.ok(data);
    }
}