package com.maotuanzi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RefundStatusEnum {
    PENDING(0, "待审核"),
    PROCESSING(1, "退款中"),
    REJECTED(2, "审核拒绝"),
    SUCCESS(3, "已退款");

    private final int code;
    private final String desc;
}