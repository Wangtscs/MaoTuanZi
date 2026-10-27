package com.maotuanzi.config;

import com.maotuanzi.interceptor.AdminAuthInterceptor;
import com.maotuanzi.interceptor.AuthInterceptor;
import com.maotuanzi.interceptor.OpsMetricsInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;
    private final OpsMetricsInterceptor opsMetricsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/v1/**")
                .excludePathPatterns(
                        "/v1/auth/wx-login",
                        "/v1/auth/refresh",
                        "/v1/webhooks/**",
                        "/v1/breeds",
                        "/v1/breeds/**",
                        "/v1/ai/styles",
                        "/v1/ai/templates",
                        "/v1/products",
                        "/v1/products/**",
                        "/v1/health"
                );

        registry.addInterceptor(opsMetricsInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/static/**",
                        "/favicon.ico",
                        "/v1/auth/wx-login",
                        "/v1/auth/refresh",
                        "/v1/health",
                        "/ops/auth/login",
                        "/ops/auth/logout",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs",
                        "/v3/api-docs"
                );

        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**", "/ops/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Trace-Id", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}