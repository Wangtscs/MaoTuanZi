package com.maotuanzi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 领取奖励结果（对齐前端契约：status）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardClaimResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 领取状态 */
    private String status;
}
