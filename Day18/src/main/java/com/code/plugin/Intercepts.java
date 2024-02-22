package com.code.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 拦截注解
 *
 * @author HeXin
 * @date 2024/02/21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Intercepts {

    /**
     * 值( Signature 类型数组)
     *
     * @return {@link Signature[]}
     */
    Signature[] value();
}
