package com.maotuanzi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AiTaskStatusEnum {
    PENDING(0, "待处理"),
    PROCESSING(1, "生成中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String desc;
}