package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.dto.ShareRecordRequest;
import com.maotuanzi.dto.ShareRecordResult;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.service.RewardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 分享记录与奖励（阶段4）。
 */
@Api(tags = "分享")
@RestController
@RequestMapping("/v1/share")
@RequireAuth
@RequiredArgsConstructor
public class ShareController extends BaseController {

    private final RewardService rewardService;

    @ApiOperation("记录一次分享（幂等发放 mock 奖励）")
    @PostMapping("/record")
    public ApiResult<ShareRecordResult> record(@Valid @RequestBody ShareRecordRequest req) {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        return ApiResult.ok(rewardService.recordShare(uid, req.getPostId(), req.getChannel()));
    }
}
