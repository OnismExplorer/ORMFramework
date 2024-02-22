package com.code.session.defaults;

import com.code.executor.Executor;
import com.code.mapping.MappedStatement;
import com.code.session.Configuration;
import com.code.session.RowBounds;
import com.code.session.SqlSession;

import java.sql.SQLException;
import java.util.List;

/**
 * 默认 SQL 会话实现类
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class DefaultSqlSession implements SqlSession {
    private final Configuration configuration;
    private final Executor executor;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        List<T> list = this.selectList(statement, parameter);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new RuntimeException("在执行 selectOne() 操作时，期望返回一个结果（或为 null），但实际结果数量为: " + list.size());
        }
        return null;
    }

    @Override
    public <T> List<T> selectList(String statement, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        return executor.query(mappedStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
    }

    @Override
    public int insert(String statement, Object parameter) {
        // insert 的本质其实就是 update 的一种包装
        return update(statement,parameter);
    }

    @Override
    public int update(String statement, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        try{
            return executor.update(mappedStatement,parameter);
        } catch (SQLException e) {
            throw new RuntimeException("更新数据时发生异常："+e);
        }
    }

    @Override
    public Object delete(String statement, Object parameter) {
        return update(statement,parameter);
    }

    @Override
    public void commit() {
        try {
            executor.commit(true);
        } catch (SQLException e) {
            throw new RuntimeException("提交事务时发生错误：" + e);
        }

    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void close() {
        executor.close(true);
    }

    @Override
    public void clearCache() {
        executor.clearLocalCache();
    }

}
