package com.code.mapping;

/**
 * SQL 源码
 *
 * @author HeXin
 * @date 2024/02/05
 */
public interface SqlSource {
    BoundSql getBoundSql(Object parameterObject);
}
