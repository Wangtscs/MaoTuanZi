package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.enums.VipLevelEnum;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.dto.InviteInfoResult;
import com.maotuanzi.dto.RewardBrief;
import com.maotuanzi.dto.RewardClaimResult;
import com.maotuanzi.dto.ShareInviteResult;
import com.maotuanzi.dto.ShareRecordResult;
import com.maotuanzi.entity.Reward;
import com.maotuanzi.entity.SquarePost;
import com.maotuanzi.entity.User;
import com.maotuanzi.entity.VipRecord;
import com.maotuanzi.mapper.RewardMapper;
import com.maotuanzi.mapper.SquareMapper;
import com.maotuanzi.mapper.UserMapper;
import com.maotuanzi.mapper.VipRecordMapper;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.service.RewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分享 / 邀请奖励服务实现（mock 奖励，幂等、可编译）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private static final String REWARD_RULE = "邀请1位得3天VIP";
    private static final String INVITE_URL_PREFIX = "maotuanzi://invite/";

    private final RewardMapper rewardMapper;
    private final SquareMapper squareMapper;
    private final UserMapper userMapper;
    private final VipRecordMapper vipRecordMapper;

    @Override
    @Transactional
    public ShareRecordResult recordShare(Long userId, Long postId, String channel) {
        int shareCount = 0;
        if (postId != null) {
            squareMapper.incrShareCount(postId, 1);
            SquarePost post = squareMapper.selectPostById(postId);
            shareCount = post == null || post.getShareCount() == null ? 0 : post.getShareCount();
        }

        // 幂等：同一用户每天仅发放一次分享奖励（10 积分，占位奖励）
        Reward existing = rewardMapper.selectPendingShareToday(userId, LocalDate.now());
        Reward reward = existing;
        if (reward == null) {
            reward = new Reward();
            reward.setUserId(userId);
            reward.setType("POINTS");
            reward.setValue(10);
            reward.setRefType("SHARE");
            reward.setRefId(postId);
            reward.setStatus(0);
            reward.setRemark("分享奖励 " + (channel == null ? "" : channel));
            reward.setExpireAt(LocalDateTime.now().plusDays(30));
            rewardMapper.insert(reward);
        }

        ShareRecordResult result = new ShareRecordResult();
        result.setShareCount(shareCount);
        result.setReward(new RewardBrief(reward.getType(), reward.getValue() == null ? 0 : reward.getValue()));
        return result;
    }

    @Override
    @Transactional
    public ShareInviteResult bindInvite(String inviterCode) {
        Long userId = UserContext.getUserId();
        if (userId == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        if (inviterCode == null || inviterCode.isBlank()) throw BizException.of(ErrorCode.PARAM_MISSING);

        User inviter = userMapper.selectByInviteCode(inviterCode.trim());
        if (inviter == null) throw BizException.of(ErrorCode.PARAM_INVALID, "邀请码无效");
        if (inviter.getId().equals(userId)) throw BizException.of(ErrorCode.STATE_CONFLICT, "不能填写自己的邀请码");

        User me = userMapper.selectById(userId);
        if (me.getInviterId() != null) throw BizException.of(ErrorCode.STATE_CONFLICT, "已绑定过邀请人");

        userMapper.updateInviter(userId, inviter.getId());

        // 向被邀请人 + 邀请人各发放 3 天会员奖励
        grantVipDaysReward(userId, "INVITE", inviter.getId(), 3);
        grantVipDaysReward(inviter.getId(), "INVITE_INVITER", userId, 3);
        log.info("绑定邀请 code={} inviter={} invitee={}", inviterCode, inviter.getId(), userId);

        return new ShareInviteResult(true);
    }

    private void grantVipDaysReward(Long userId, String refType, Long refId, int days) {
        Reward r = new Reward();
        r.setUserId(userId);
        r.setType("VIP_DAYS");
        r.setValue(days);
        r.setRefType(refType);
        r.setRefId(refId);
        r.setStatus(0);
        r.setRemark("邀请奖励 " + days + " 天会员");
        r.setExpireAt(LocalDateTime.now().plusDays(30));
        rewardMapper.insert(r);
    }

    @Override
    @Transactional
    public RewardClaimResult claim(Long userId, String rewardId) {
        Long id = parseRewardId(rewardId);
        Reward r = rewardMapper.selectById(id);
        if (r == null || !r.getUserId().equals(userId)) throw BizException.of(ErrorCode.NOT_FOUND);
        if (r.getStatus() == null || r.getStatus() != 0) {
            // 幂等：已领取仍返回 CLAIMED
            return new RewardClaimResult("CLAIMED");
        }

        r.setStatus(1);
        r.setClaimedAt(LocalDateTime.now());
        rewardMapper.updateClaimed(r);

        if ("VIP_DAYS".equals(r.getType())) {
            applyVipDays(userId, r.getValue(), r.getRemark());
        } else {
            // COUPON / POINTS：占位奖励，仅记账（积分/卡券账本后续接入）
            log.info("领取占位奖励 user_id={} type={} value={}", userId, r.getType(), r.getValue());
        }
        return new RewardClaimResult("CLAIMED");
    }

    private Long parseRewardId(String rewardId) {
        if (rewardId == null || rewardId.isBlank()) throw BizException.of(ErrorCode.PARAM_MISSING);
        try {
            return Long.parseLong(rewardId.trim());
        } catch (NumberFormatException e) {
            throw BizException.of(ErrorCode.PARAM_INVALID, "奖励 ID 非法");
        }
    }

    private void applyVipDays(Long userId, int days, String remark) {
        User u = userMapper.selectById(userId);
        if (u == null) return;
        LocalDateTime base = (u.getVipExpireAt() != null && u.getVipExpireAt().isAfter(LocalDateTime.now()))
                ? u.getVipExpireAt() : LocalDateTime.now();
        LocalDateTime newExpire = base.plusDays(days);
        int newLevel = Math.max(u.getVipLevel() == null ? 0 : u.getVipLevel(), VipLevelEnum.MONTH.getCode());

        userMapper.updateVip(u.getId(), newLevel, newExpire);

        VipRecord rec = new VipRecord();
        rec.setUserId(u.getId());
        rec.setChangeType(2); // 2 = 奖励发放
        rec.setVipLevelBefore(u.getVipLevel());
        rec.setVipLevelAfter(newLevel);
        rec.setExpireBefore(u.getVipExpireAt());
        rec.setExpireAfter(newExpire);
        rec.setDurationDays(days);
        rec.setAmountCents(0);
        rec.setRemark(remark);
        vipRecordMapper.insert(rec);

        log.info("奖励发放会员 user_id={} days={} expire={}", u.getId(), days, newExpire);
    }

    @Override
    public List<Reward> listMine(Long userId) {
        return rewardMapper.selectByUserId(userId, null);
    }

    @Override
    public InviteInfoResult inviteInfo(Long userId) {
        User u = userMapper.selectById(userId);
        String code = u == null ? null : u.getInviteCode();
        return InviteInfoResult.builder()
                .inviteCode(code)
                .inviteUrl(code == null ? null : INVITE_URL_PREFIX + code)
                .rewardRule(REWARD_RULE)
                .build();
    }
}
