package com.maotuanzi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 退款申请结果（对齐前端契约：refundId / status）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 退款单号 */
    private String refundId;

    /** 退款状态（对齐契约，如 REFUNDING） */
    private String status;
}
