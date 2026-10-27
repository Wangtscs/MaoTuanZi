package com.maotuanzi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WorkStatusEnum {
    GENERATING(0, "生成中"),
    PREVIEW(1, "预览态"),
    COMPLETED(2, "已完成"),
    DELETED(3, "已删除");

    private final int code;
    private final String desc;
}