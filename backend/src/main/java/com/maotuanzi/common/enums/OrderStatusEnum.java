package com.maotuanzi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    COMPLETED(2, "已完成"),
    CLOSED(3, "已关闭"),
    REFUNDING(4, "退款中"),
    REFUNDED(5, "已退款"),
    REFUND_REJECTED(6, "拒绝退款");

    private final int code;
    private final String desc;
}