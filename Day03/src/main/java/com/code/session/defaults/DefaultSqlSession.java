package com.code.session.defaults;

import com.code.binding.MapperRegistry;
import com.code.mapping.MappedStatement;
import com.code.session.Configuration;
import com.code.session.SqlSession;

/**
 * 默认 SQL 会话实现类
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class DefaultSqlSession implements SqlSession {

    /**
     * 配置类
     */
    private final Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T selectOne(String statement) {
        return (T) (statement+"被代理！");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T selectOne(String statement, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        return (T) ("入参为 "+parameter+" 的 "+statement+" 方法被代理！待执行 SQL 语句："+mappedStatement.getSql());
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
