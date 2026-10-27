package com.maotuanzi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卡券领取结果（对齐前端契约：id / status）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponClaimResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 领取记录 ID（字符串） */
    private String id;

    /** 领取状态 */
    private String status;
}
