package com.maotuanzi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要管理员登录
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {

    /**
     * 需要的权限 code，留空表示仅登录
     */
    String permission() default "";
}