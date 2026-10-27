package com.maotuanzi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 业务范围 login/upload/refund/ai-task */
    String scope();

    /** 限流上限 */
    long limit() default 10;

    /** 时间窗口（秒） */
    long windowSeconds() default 60;

    /** 自定义 key 维度（用户 ID 默认） */
    String key() default "";
}