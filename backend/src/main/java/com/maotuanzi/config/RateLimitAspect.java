package com.maotuanzi.config;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 限流切面（基于 Redis 计数）
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redis;

    @Value("${maotuanzi.rate-limit.login-per-hour:10}")
    private int loginPerHour;

    @Value("${maotuanzi.rate-limit.upload-per-minute:30}")
    private int uploadPerMinute;

    @Value("${maotuanzi.rate-limit.refund-per-month:1}")
    private int refundPerMonth;

    @Around("@annotation(com.maotuanzi.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        com.maotuanzi.annotation.RateLimit ann = ((org.aspectj.lang.reflect.MethodSignature) pjp.getSignature())
                .getMethod().getAnnotation(com.maotuanzi.annotation.RateLimit.class);

        String scope = ann.scope();
        long limit = ann.limit();
        Duration window = Duration.ofSeconds(ann.windowSeconds());
        String key;

        switch (scope) {
            case "login":
                key = "rl:login:" + currentKey(ann.key());
                limit = loginPerHour;
                window = Duration.ofHours(1);
                break;
            case "upload":
                key = "rl:upload:" + currentKey(ann.key());
                limit = uploadPerMinute;
                window = Duration.ofMinutes(1);
                break;
            case "refund":
                key = "rl:refund:" + currentKey(ann.key());
                limit = refundPerMonth;
                window = Duration.ofDays(30);
                break;
            default:
                key = "rl:" + scope + ":" + currentKey(ann.key());
                break;
        }

        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, window);
        }
        if (count != null && count > limit) {
            throw BizException.of(ErrorCode.RATE_LIMIT_USER);
        }
        return pjp.proceed();
    }

    private String currentKey(String customKey) {
        if (customKey != null && !customKey.isEmpty()) return customKey;
        Long userId = com.maotuanzi.security.UserContext.getUserId();
        if (userId != null) return "u:" + userId;
        return "ip:unknown";
    }
}