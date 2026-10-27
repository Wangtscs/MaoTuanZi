package com.maotuanzi.ops;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 运维模块时间与分页工具
 */
public final class OpsTimeUtil {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private OpsTimeUtil() {
    }

    /** 宽松解析时间字符串，支持 yyyy-MM-dd / yyyy-MM-dd HH:mm:ss / ISO */
    public static LocalDateTime parse(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String v = s.trim().replace('T', ' ');
        try {
            if (v.length() <= 10) return LocalDate.parse(v, D).atStartOfDay();
            if (v.length() == 16) v = v + ":00";
            if (v.length() > 19) v = v.substring(0, 19);
            return LocalDateTime.parse(v, DT);
        } catch (Exception e) {
            return null;
        }
    }

    public static String format(LocalDateTime t) {
        return t == null ? null : t.format(DT);
    }

    /** 构造分页信息 */
    public static Map<String, Object> pagination(int page, int pageSize, long total) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("page", page);
        p.put("pageSize", pageSize);
        p.put("total", total);
        p.put("hasMore", (long) page * pageSize < total);
        return p;
    }

    /** 组装 {list, pagination} */
    public static Map<String, Object> page(Object list, int page, int pageSize, long total) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("list", list);
        m.put("pagination", pagination(page, pageSize, total));
        return m;
    }

    public static int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    public static int normalizeSize(Integer pageSize, int def, int max) {
        if (pageSize == null || pageSize < 1) return def;
        return Math.min(pageSize, max);
    }
}
