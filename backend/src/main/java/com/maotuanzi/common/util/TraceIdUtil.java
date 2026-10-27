package com.maotuanzi.common.util;

import cn.hutool.core.util.IdUtil;
import org.slf4j.MDC;

/**
 * TraceId 工具
 */
public final class TraceIdUtil {

    private static final String KEY = "traceId";

    private TraceIdUtil() {}

    public static String get() {
        String id = MDC.get(KEY);
        if (id == null) {
            id = IdUtil.fastSimpleUUID();
            MDC.put(KEY, id);
        }
        return id;
    }

    public static void set(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(KEY, traceId);
        } else {
            get();
        }
    }

    public static void clear() {
        MDC.remove(KEY);
    }
}