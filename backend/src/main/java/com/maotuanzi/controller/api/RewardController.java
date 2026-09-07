package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.dto.ClaimRequest;
import com.maotuanzi.dto.InviteInfoResult;
import com.maotuanzi.dto.InviteRequest;
import com.maotuanzi.dto.RewardClaimResult;
import com.maotuanzi.dto.ShareInviteResult;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.service.RewardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 奖励 / 邀请（阶段4）。
 */
@Api(tags = "奖励/邀请")
@RestController
@RequireAuth
@RequiredArgsConstructor
public class RewardController extends BaseController {

    private final RewardService rewardService;

    @ApiOperation("绑定邀请码（邀请人+被邀请人各得奖励）")
    @PostMapping("/v1/share/invite")
    public ApiResult<ShareInviteResult> invite(@Valid @RequestBody InviteRequest req) {
        if (UserContext.getUserId() == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        return ApiResult.ok(rewardService.bindInvite(req.getInviterCode()));
    }

    @ApiOperation("领取奖励")
    @PostMapping("/v1/rewards/claim")
    public ApiResult<RewardClaimResult> claim(@Valid @RequestBody ClaimRequest req) {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        return ApiResult.ok(rewardService.claim(uid, req.getRewardId()));
    }

    @ApiOperation("我的邀请信息（邀请码/深链/奖励规则）")
    @GetMapping("/v1/user/invite")
    public ApiResult<InviteInfoResult> inviteInfo() {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        return ApiResult.ok(rewardService.inviteInfo(uid));
    }
}
