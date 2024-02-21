package com.code.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 插入(insert)语句注解
 *
 * @author HeXin
 * @date 2024/02/14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Insert {

    /**
     * SQL 语句内容
     *
     * @return {@link String[]}
     */
    String[] value();
}
