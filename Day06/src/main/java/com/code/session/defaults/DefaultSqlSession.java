package com.code.session.defaults;

import com.code.executor.Executor;
import com.code.mapping.BoundSql;
import com.code.mapping.Environment;
import com.code.mapping.MappedStatement;
import com.code.session.Configuration;
import com.code.session.SqlSession;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认 SQL 会话实现类
 *
 * @param configuration 配置类
 * @author HeXin
 * @date 2024/01/25
 */
public record DefaultSqlSession(Configuration configuration,Executor executor) implements SqlSession {


    @Override
    @SuppressWarnings("unchecked")
    public <T> T selectOne(String statement) {
        return this.selectOne(statement,null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        List<T> list = executor.query(mappedStatement, parameter, Executor.NO_RESULT_HANDLER, mappedStatement.getBoundSql());
        return list.get(0);
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

}
