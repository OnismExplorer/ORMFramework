package com.code.annotations;

/**
 * 更新(update)语句注解
 *
 * @author HeXin
 * @date 2024/02/14
 */
public @interface Update {

    /**
     * SQL 语句内容
     *
     * @return {@link String[]}
     */
    String[] value();
}
