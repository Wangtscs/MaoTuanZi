package com.maotuanzi.security;

import java.util.Set;

/**
 * 管理员上下文
 */
public final class AdminContext {

    private static final ThreadLocal<Long> ADMIN_ID = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> PERMS = new ThreadLocal<>();

    private AdminContext() {}

    public static void set(Long adminId, Set<String> perms) {
        ADMIN_ID.set(adminId);
        PERMS.set(perms);
    }

    public static Long getAdminId() {
        return ADMIN_ID.get();
    }

    public static Set<String> getPerms() {
        return PERMS.get();
    }

    public static boolean hasPerm(String code) {
        Set<String> p = PERMS.get();
        return AdminPermissions.allows(p, code);
    }

    public static void clear() {
        ADMIN_ID.remove();
        PERMS.remove();
    }
}
