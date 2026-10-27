package com.maotuanzi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostStatusEnum {
    REVIEWING(0, "审核中"),
    PUBLISHED(1, "已发布"),
    OFFLINE(2, "已下架"),
    DELETED(3, "已删除");

    private final int code;
    private final String desc;
}