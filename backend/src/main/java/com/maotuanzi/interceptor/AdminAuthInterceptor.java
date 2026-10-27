package com.maotuanzi.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.security.AdminContext;
import com.maotuanzi.security.AdminPermissions;
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
import java.util.Set;

/**
 * 管理员鉴权拦截器
 */
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    public static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";
    public static final String ADMIN_TOKEN_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;

        HandlerMethod hm = (HandlerMethod) handler;
        Method method = hm.getMethod();
        Class<?> bean = hm.getBeanType();

        RequireAdmin ann = method.getAnnotation(RequireAdmin.class);
        if (ann == null) ann = bean.getAnnotation(RequireAdmin.class);
        if (ann == null) return true;

        String header = request.getHeader(ADMIN_TOKEN_HEADER);
        if (header == null) header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(ADMIN_TOKEN_PREFIX)) {
            writeError(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        String token = header.substring(ADMIN_TOKEN_PREFIX.length()).trim();
        if (Boolean.TRUE.equals(redisTemplate.hasKey("admin:token:invalid:" + token))) {
            writeError(response, ErrorCode.TOKEN_INVALID);
            return false;
        }

        try {
            Claims claims = jwtUtil.parse(token);
            Object rawId = claims.get("adminId");
            if (!(rawId instanceof Number) || ((Number) rawId).longValue() <= 0) {
                writeError(response, ErrorCode.TOKEN_INVALID);
                return false;
            }
            Long adminId = ((Number) rawId).longValue();
            Set<String> perms = AdminPermissions.normalize(claims.get("perms"));
            AdminContext.set(adminId, perms);

            String need = ann.permission();
            if (!need.isEmpty() && !AdminPermissions.allows(perms, need)) {
                AdminContext.clear();
                writeError(response, ErrorCode.FORBIDDEN);
                return false;
            }
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
        AdminContext.clear();
    }

    private void writeError(HttpServletResponse response, ErrorCode ec) throws IOException {
        response.setStatus(ec.getHttpStatus());
        response.setContentType("application/json;charset=UTF-8");
        ApiResult<Void> r = ApiResult.fail(ec.getCode(), ec.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(r));
    }
}
