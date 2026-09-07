package com.maotuanzi.service;

import com.maotuanzi.dto.InviteInfoResult;
import com.maotuanzi.dto.RewardClaimResult;
import com.maotuanzi.dto.ShareInviteResult;
import com.maotuanzi.dto.ShareRecordResult;
import com.maotuanzi.entity.Reward;

import java.util.List;

/**
 * 分享 / 邀请奖励服务。
 */
public interface RewardService {

    /**
     * 记录一次分享，并（幂等地）发放每日首次分享奖励。
     *
     * @param userId  当前用户
     * @param postId  关联的帖子 ID，可为空
     * @param channel 分享渠道
     * @return 分享结果与奖励
     */
    ShareRecordResult recordShare(Long userId, Long postId, String channel);

    /**
     * 绑定邀请码：给当前登录用户记录邀请人，并向邀请人 + 被邀请人各发一份奖励。
     *
     * @param inviterCode 邀请人的邀请码
     * @return 是否已发奖
     */
    ShareInviteResult bindInvite(String inviterCode);

    /**
     * 领取奖励并应用效果（如延长会员天数）。
     *
     * @param userId   当前用户
     * @param rewardId 奖励 ID（字符串，内部转为 Long）
     * @return 领取结果
     */
    RewardClaimResult claim(Long userId, String rewardId);

    /**
     * 当前用户的奖励列表（内部/后续管理用）。
     */
    List<Reward> listMine(Long userId);

    /**
     * 当前用户的邀请信息：邀请码、邀请深链、奖励规则。
     */
    InviteInfoResult inviteInfo(Long userId);
}
