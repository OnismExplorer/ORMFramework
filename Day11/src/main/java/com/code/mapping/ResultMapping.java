package com.code.mapping;

import com.code.session.Configuration;
import com.code.type.JdbcType;
import com.code.type.TypeHandler;

/**
 * 结果映射
 *
 * @author HeXin
 * @date 2024/02/12
 */
public class ResultMapping {
    private Configuration configuration;

    private String property;

    private String column;

    private Class<?> javaType;

    private JdbcType jdbcType;

    private TypeHandler<?> typeHandler;

    ResultMapping() {
    }

    public static class Builder {
        private ResultMapping resultMapping = new ResultMapping();
    }
}
