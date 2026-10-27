package com.maotuanzi.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.common.constant.CommonConstants;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 用户鉴权拦截器
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;

        HandlerMethod hm = (HandlerMethod) handler;
        Method method = hm.getMethod();
        Class<?> bean = hm.getBeanType();

        RequireAuth auth = method.getAnnotation(RequireAuth.class);
        if (auth == null) auth = bean.getAnnotation(RequireAuth.class);

        if (auth == null || !auth.required()) return true;

        String header = request.getHeader(CommonConstants.AUTH_HEADER);
        if (header == null || !header.startsWith(CommonConstants.AUTH_BEARER)) {
            writeError(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        String token = header.substring(CommonConstants.AUTH_BEARER.length()).trim();
        if (Boolean.TRUE.equals(redisTemplate.hasKey("token:invalid:" + token))) {
            writeError(response, ErrorCode.TOKEN_INVALID);
            return false;
        }

        try {
            Claims claims = jwtUtil.parse(token);
            Long userId = claims.get("uid", Number.class).longValue();
            String openid = claims.get("openid", String.class);
            Integer vip = claims.get("vip", Integer.class);
            UserContext.set(userId, openid, vip);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            writeError(response, ErrorCode.TOKEN_EXPIRED);
            return false;
        } catch (Exception e) {
            writeError(response, ErrorCode.TOKEN_INVALID);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeError(HttpServletResponse response, ErrorCode ec) throws IOException {
        response.setStatus(ec.getHttpStatus());
        response.setContentType("application/json;charset=UTF-8");
        ApiResult<Void> r = ApiResult.fail(ec.getCode(), ec.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(r));
    }
}