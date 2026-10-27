package com.maotuanzi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatusEnum {
    NORMAL(0, "正常"),
    BANNED(1, "封禁"),
    CANCELLED(2, "注销");

    private final int code;
    private final String desc;
}