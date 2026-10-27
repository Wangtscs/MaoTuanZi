package com.maotuanzi.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ID 生成工具
 */
public final class IdUtil {

    private IdUtil() {}

    private static final DateTimeFormatter ORDER_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final String ORDER_PREFIX = "MT";

    /**
     * 生成订单号：MT + 时间戳 + 4 位随机
     */
    public static String genOrderNo() {
        return ORDER_PREFIX + LocalDateTime.now().format(ORDER_FMT) + RandomUtil.randomNumbers(4);
    }

    /**
     * 生成退款单号：RF + 时间戳 + 4 位随机
     */
    public static String genRefundNo() {
        return "RF" + LocalDateTime.now().format(ORDER_FMT) + RandomUtil.randomNumbers(4);
    }

    /**
     * 雪花 ID 简化版（依赖 hutool）
     */
    public static long snowflakeId() {
        return cn.hutool.core.util.IdUtil.getSnowflake(1, 1).nextId();
    }

    public static String genIdempotencyKey() {
        return cn.hutool.core.util.IdUtil.randomUUID();
    }

    public static String now() {
        return DateUtil.now();
    }
}