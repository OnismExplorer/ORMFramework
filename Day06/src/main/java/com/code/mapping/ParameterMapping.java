package com.code.mapping;

import com.code.session.Configuration;
import com.code.type.JdbcType;

/**
 * 参数映射
 *
 * @author HeXin
 * @date 2024/01/26
 */
public class ParameterMapping {
    private Configuration configuration;

    /**
     * property
     */
    private String property;
    /**
     * javaType = int
     */
    private Class<?> javaType = Object.class;
    /**
     * JDBC 类型 = NUMERIC
     */
    private JdbcType jdbcType;

    private ParameterMapping() {
    }

    public static class Builder {

        private final ParameterMapping parameterMapping = new ParameterMapping();

        public Builder(Configuration configuration, String property) {
            parameterMapping.configuration = configuration;
            parameterMapping.property = property;
        }

        public Builder javaType(Class<?> javaType) {
            parameterMapping.javaType = javaType;
            return this;
        }

        public Builder jdbcType(JdbcType jdbcType) {
            parameterMapping.jdbcType = jdbcType;
            return this;
        }

    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getProperty() {
        return property;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }

}
