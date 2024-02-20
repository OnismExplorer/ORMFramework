package com.code.builder;

import com.code.mapping.ParameterMapping;
import com.code.mapping.SqlSource;
import com.code.parsing.GenericTokenParser;
import com.code.parsing.TokenHandler;
import com.code.reflection.MetaClass;
import com.code.reflection.MetaObject;
import com.code.session.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL源代码构建器
 *
 * @author HeXin
 * @date 2024/02/05
 */
public class SqlSourceBuilder extends BaseBuilder{

    /**
     * 参数属性
     */
    private static final String parameterProperties = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";
    public SqlSourceBuilder(Configuration configuration) {
        super(configuration);
    }

    private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {
        /**
         * 参数映射
         */
        private List<ParameterMapping> parameterMappings = new ArrayList<>();
        /**
         * 参数类型
         */
        private Class<?> parameterType;
        /**
         * 元参数
         */
        private final MetaObject metaParameters;

        public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType, Map<String, Object> additionalParameters) {
            super(configuration);
            this.parameterType = parameterType;
            this.metaParameters = configuration.newMetaObject(additionalParameters);
        }

        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }

        @Override
        public String handleToken(String content) {
            parameterMappings.add(buildParameterMapping(content));
            return "?";
        }

        /**
         * 构建参数映射对象。
         *
         * @param content 参数表达式的内容
         * @return 参数映射对象
         */
        private ParameterMapping buildParameterMapping(String content) {
            // 解析参数表达式
            Map<String,String> propertiesMap = new ParameterExpression(content);

            // 获取参数的属性名
            String property = propertiesMap.get("property");

            // 确定参数的类型
            Class<?> propertyType;
            // 如果已注册对应参数类型的类型处理器
            if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
                propertyType = parameterType;
            } else if (property != null) {
                // 如果存在属性名，则通过反射获取属性的类型
                MetaClass metaClass = MetaClass.forClass(parameterType);
                if (metaClass.hasGetter(property)) {
                    propertyType = metaClass.getGetterType(property);
                } else {
                    propertyType = Object.class;
                }
            } else {
                // 如果没有注册对应参数类型的类型处理器，也没有属性名，则默认为 Object 类型
                propertyType = Object.class;
            }

            // 使用构建器创建 ParameterMapping 对象
            ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
            return builder.build();
        }
    }


        /**
     * 解析 SQL 字符串，替换其中的参数占位符，并返回 SqlSource 对象。
     *
     * @param originalSql 原始 SQL 字符串
     * @param parameterType 参数类型
     * @param additionalParameters 附加的参数映射
     * @return 解析后的 SqlSource 对象
     */
    public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
        // 创建 ParameterMappingTokenHandler 对象，用于处理参数占位符
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType, additionalParameters);

        // 创建 GenericTokenParser 对象，用于解析 SQL 字符串
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);

        // 解析 SQL 字符串，替换其中的参数占位符
        String sql = parser.parse(originalSql);

        // 返回静态 SQL
        return new StaticSqlSource(sql, handler.getParameterMappings(), configuration);
    }
}
