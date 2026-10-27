package com.maotuanzi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 我的邀请信息（对齐前端契约：inviteCode / inviteUrl / rewardRule）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteInfoResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 我的邀请码 */
    private String inviteCode;

    /** 邀请深链 */
    private String inviteUrl;

    /** 奖励规则文案 */
    private String rewardRule;
}
