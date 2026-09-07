package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分享 / 邀请奖励。
 */
@Data
public class Reward implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    /** 奖励类型：VIP_DAYS（会员天数） / COUPON（卡券） / POINTS（积分） */
    private String type;
    /** 奖励数值：VIP_DAYS 为天数，COUPON 为卡券 ID，POINTS 为积分数 */
    private Integer value;
    /** 来源：SHARE / INVITE / INVITE_INVITER */
    private String refType;
    /** 来源关联 ID：帖子 ID / 邀请人 ID 等 */
    private Long refId;
    /** 状态：0 待领取 1 已领取 2 已过期 */
    private Integer status;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime expireAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime claimedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}
