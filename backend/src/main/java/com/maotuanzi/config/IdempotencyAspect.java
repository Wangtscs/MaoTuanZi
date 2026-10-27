package com.maotuanzi.config;

import com.maotuanzi.annotation.Idempotent;
import com.maotuanzi.common.constant.CommonConstants;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.mapper.IdempotencyMapper;
import com.maotuanzi.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 幂等性拦截
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {

    private final IdempotencyMapper idempotencyMapper;
    private final StringRedisTemplate redis;

    @Around("@annotation(idem)")
    public Object around(ProceedingJoinPoint pjp, Idempotent idem) throws Throwable {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String key = req.getHeader(CommonConstants.IDEMPOTENCY_HEADER);
        if (key == null || key.isEmpty()) {
            if (idem.required()) {
                throw BizException.of(ErrorCode.PARAM_MISSING, "缺少幂等键 Idempotency-Key");
            }
            return pjp.proceed();
        }

        String cacheKey = "idempotent:" + key;
        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("幂等命中 key={}", key);
            return cached;
        }

        Object result = pjp.proceed();
        String resp = result == null ? "" : result.toString();

        try {
            idempotencyMapper.insert(key, UserContext.getUserId(), req.getRequestURI(), resp,
                    LocalDateTime.now().plusSeconds(idem.expireSeconds()));
        } catch (Exception ignore) {}

        redis.opsForValue().set(cacheKey, resp, java.time.Duration.ofSeconds(idem.expireSeconds()));
        return result;
    }
}