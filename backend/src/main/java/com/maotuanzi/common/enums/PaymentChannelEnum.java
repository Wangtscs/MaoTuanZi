package com.maotuanzi.common.enums;

import lombok.Getter;

/**
 * 支付渠道枚举
 */
@Getter
public enum PaymentChannelEnum {
    WECHAT("WECHAT"),
    HUAWEI("HUAWEI");

    private final String code;

    PaymentChannelEnum(String code) {
        this.code = code;
    }

    public static boolean isSupported(String channel) {
        if (channel == null) return false;
        for (PaymentChannelEnum e : values()) {
            if (e.code.equalsIgnoreCase(channel)) return true;
        }
        return false;
    }
}
