package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RateLimit;
import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.dto.LoginResponse;
import com.maotuanzi.dto.WxLoginRequest;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = "认证")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final UserService userService;

    @ApiOperation("微信一键登录")
    @PostMapping("/wx-login")
    @RateLimit(scope = "login", limit = 10, windowSeconds = 3600)
    public ApiResult<LoginResponse> wxLogin(@Valid @RequestBody WxLoginRequest req) {
        return ApiResult.ok(userService.wxLogin(req));
    }

    @ApiOperation("刷新 Token")
    @PostMapping("/refresh")
    public ApiResult<LoginResponse> refresh(@RequestBody java.util.Map<String, String> body) {
        String rt = body.get("refresh_token");
        if (rt == null || rt.isEmpty()) throw BizException.of(ErrorCode.PARAM_MISSING);
        return ApiResult.ok(userService.refresh(rt));
    }

    @ApiOperation("登出")
    @PostMapping("/logout")
    public ApiResult<Void> logout(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            userService.logout(header.substring(7));
        }
        return ApiResult.ok();
    }
}