package com.maotuanzi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VipLevelEnum {
    NONE(0, "普通用户", 0),
    MONTH(1, "月卡会员", 30),
    QUARTER(2, "季卡会员", 90),
    YEAR(3, "年卡会员", 365);

    private final int code;
    private final String name;
    private final int days;

    public static VipLevelEnum of(Integer code) {
        if (code == null) return NONE;
        for (VipLevelEnum v : values()) {
            if (v.code == code) return v;
        }
        return NONE;
    }
}