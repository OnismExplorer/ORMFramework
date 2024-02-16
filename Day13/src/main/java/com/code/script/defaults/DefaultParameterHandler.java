package com.code.script.defaults;

import com.code.executor.parameter.ParameterHandler;
import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.mapping.ParameterMapping;
import com.code.reflection.MetaObject;
import com.code.session.Configuration;
import com.code.type.JdbcType;
import com.code.type.TypeHandler;
import com.code.type.TypeHandlerRegistry;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * 默认参数处理器
 *
 * @author HeXin
 * @date 2024/02/12
 */
public class DefaultParameterHandler implements ParameterHandler {

    private final TypeHandlerRegistry typeHandlerRegistry;

    private final MappedStatement mappedStatement;

    private final Object parameterObject;

    private BoundSql boundSql;

    private Configuration configuration;

    public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        this.mappedStatement = mappedStatement;
        this.parameterObject = parameterObject;
        this.boundSql = boundSql;
        this.configuration = mappedStatement.getConfiguration();
    }

    @Override
    public Object getParameterObject() {
        return parameterObject;
    }

    /**
     * 设置 PreparedStatement 的参数。
     *
     * @param preparedStatement PreparedStatement 对象
     * @throws SQLException 如果 SQL 操作发生异常
     */
    @Override
    public void setParameters(PreparedStatement preparedStatement) throws SQLException {
        // 获取参数映射列表
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        // 遍历参数映射列表
        for (int i = 0; i < parameterMappings.size(); i++) {
            // 获取当前参数映射
            ParameterMapping parameterMapping = parameterMappings.get(i);

            // 获取属性名
            String propertyName = parameterMapping.getProperty();

            // 获取参数值
            Object value;
            // 如果已注册对应参数类型的类型处理器
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                value = parameterObject;
            } else {
                // 否则，通过元对象获取属性值
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                value = metaObject.getValue(propertyName);
            }

            // 获取 JDBC 类型
            JdbcType jdbcType = parameterMapping.getJdbcType();

            // 获取参数类型处理器
            TypeHandler typeHandler = parameterMapping.getTypeHandler();

            // 设置参数值到 PreparedStatement
            typeHandler.setParameter(preparedStatement, i + 1, value, jdbcType);
        }
    }

}
