package com.maotuanzi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductTypeEnum {
    SINGLE(1, "单次付费"),
    SUBSCRIPTION(2, "订阅"),
    PACKAGE(3, "套餐");

    private final int code;
    private final String desc;
}