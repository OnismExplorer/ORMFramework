package com.code.session.defaults;

import com.code.binding.MapperRegistry;
import com.code.session.SqlSession;
import com.code.session.SqlSessionFactory;

/**
 * 默认 SQL 会话工厂，用于获取DefaultSqlSession
 *
 * @author HeXin
 * @date 2024/01/21
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {
    private final MapperRegistry mapperRegistry;

    public DefaultSqlSessionFactory(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(mapperRegistry);
    }
}
