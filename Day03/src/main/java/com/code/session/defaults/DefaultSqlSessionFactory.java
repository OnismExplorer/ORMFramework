package com.code.session.defaults;

import com.code.session.Configuration;
import com.code.session.SqlSession;
import com.code.session.SqlSessionFactory;

/**
 * 默认 SQL 会话工厂，用于获取DefaultSqlSession
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {
    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configurationn) {
        this.configuration = configurationn;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(configuration);
    }
}
