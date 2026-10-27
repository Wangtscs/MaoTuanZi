package com.maotuanzi.security;

/**
 * 当前登录用户上下文（ThreadLocal）
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> OPENID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> VIP_LEVEL = new ThreadLocal<>();

    private UserContext() {}

    public static void set(Long userId, String openid, Integer vipLevel) {
        USER_ID.set(userId);
        OPENID.set(openid);
        VIP_LEVEL.set(vipLevel);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getOpenid() {
        return OPENID.get();
    }

    public static Integer getVipLevel() {
        return VIP_LEVEL.get();
    }

    public static void clear() {
        USER_ID.remove();
        OPENID.remove();
        VIP_LEVEL.remove();
    }
}