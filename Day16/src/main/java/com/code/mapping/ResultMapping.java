package com.code.mapping;

import com.code.session.Configuration;
import com.code.type.JdbcType;
import com.code.type.TypeHandler;
import com.code.type.TypeHandlerRegistry;

import java.util.ArrayList;
import java.util.List;

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

    private TypeHandler<?> typeHandler;

    private List<ResultFlag> flags;

    ResultMapping() {
    }

    public static class Builder {
        private ResultMapping resultMapping = new ResultMapping();

        public Builder(Configuration configuration,String property,String column,Class<?> javaType) {
            resultMapping.configuration = configuration;
            resultMapping.property = property;
            resultMapping.column = column;
            resultMapping.javaType = javaType;
            resultMapping.flags = new ArrayList<>();
        }

        /**
         * 类型处理器
         *
         * @param typeHandler 类型处理程序
         * @return {@link Builder}
         */
        public Builder typeHandler(TypeHandler<?> typeHandler) {
            resultMapping.typeHandler = typeHandler;
            return this;
        }

        public Builder flags(List<ResultFlag> flags) {
            resultMapping.flags = flags;
            return this;
        }

        public ResultMapping build(){
            resolveTypeHandler();
            return resultMapping;
        }

        /**
         * 解析类型处理程序
         */
        private void resolveTypeHandler() {
            if(resultMapping.typeHandler == null && resultMapping.javaType != null) {
                Configuration configuration = resultMapping.configuration;
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                resultMapping.typeHandler = typeHandlerRegistry.getTypeHandler(resultMapping.javaType,null);
            }
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getProperty() {
        return property;
    }

    public String getColumn() {
        return column;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    public List<ResultFlag> getFlags() {
        return flags;
    }
}
