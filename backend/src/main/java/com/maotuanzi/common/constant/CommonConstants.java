package com.maotuanzi.common.constant;

/**
 * 通用常量
 */
public final class CommonConstants {

    private CommonConstants() {}

    public static final String TRACE_ID = "X-Trace-Id";
    public static final String USER_ID = "userId";
    public static final String OPENID = "openid";
    public static final String VIP_LEVEL = "vipLevel";

    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_BEARER = "Bearer ";

    public static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    public static final long JWT_DEFAULT_EXPIRE = 7 * 24 * 60 * 60L;       // 7 天
    public static final long REFRESH_TOKEN_EXPIRE = 30 * 24 * 60 * 60L;   // 30 天

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
}