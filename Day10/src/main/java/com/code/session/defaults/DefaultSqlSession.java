package com.code.session.defaults;

import com.code.executor.Executor;
import com.code.mapping.MappedStatement;
import com.code.session.Configuration;
import com.code.session.RowBounds;
import com.code.session.SqlSession;

import java.util.List;

/**
 * 默认 SQL 会话实现类
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class DefaultSqlSession implements SqlSession {
    private Configuration configuration;
    private Executor executor;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.selectOne(statement,null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        List<T> list = executor.query(mappedStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, mappedStatement.getSqlSource().getBoundSql(parameter));
        return list.get(0);
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
