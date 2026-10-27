package com.maotuanzi.interceptor;

import com.maotuanzi.ops.OpsHttpMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP 指标采集拦截器（全局，先于业务逻辑执行）
 */
@Component
@RequiredArgsConstructor
public class OpsMetricsInterceptor implements HandlerInterceptor {

    private static final String ATTR_START = "ops.start.nano";

    private final OpsHttpMetrics httpMetrics;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATTR_START, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        Object start = request.getAttribute(ATTR_START);
        if (start == null) return;
        long costMs = (System.nanoTime() - (Long) start) / 1_000_000L;
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String errorType = ex == null ? null : ex.getClass().getSimpleName();
        httpMetrics.record(request.getMethod(),
                pattern == null ? null : String.valueOf(pattern),
                request.getRequestURI(),
                response.getStatus(),
                costMs,
                errorType);
    }
}
