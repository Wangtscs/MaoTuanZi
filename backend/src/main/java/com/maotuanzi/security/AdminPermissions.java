package com.maotuanzi.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/** Shared permission semantics for JWT collections, contexts and operations endpoints. */
public final class AdminPermissions {
    private AdminPermissions() {}

    public static Set<String> normalize(Object raw) {
        Set<String> result = new LinkedHashSet<>();
        if (raw instanceof Collection<?>) {
            for (Object item : (Collection<?>) raw) {
                if (item instanceof String && !((String) item).trim().isEmpty()) {
                    result.add(((String) item).trim());
                }
            }
        }
        return result;
    }

    public static boolean allows(Collection<String> permissions, String required) {
        if (permissions == null) return false;
        if (required == null || required.isEmpty()) return true;
        if (permissions.contains("*") || permissions.contains(required)) return true;
        int separator = required.lastIndexOf('.');
        while (separator > 0) {
            if (permissions.contains(required.substring(0, separator) + ".*")) return true;
            separator = required.lastIndexOf('.', separator - 1);
        }
        return false;
    }
}
