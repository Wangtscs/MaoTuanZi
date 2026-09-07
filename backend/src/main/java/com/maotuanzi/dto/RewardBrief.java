package com.maotuanzi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 奖励简要信息（对齐前端契约：type / amount）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardBrief implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 奖励类型 COUPON / POINTS */
    private String type;

    /** 奖励数量 */
    private int amount;
}
