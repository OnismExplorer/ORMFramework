package com.code.executor;

import com.code.executor.statement.StatementHandler;
import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.session.Configuration;
import com.code.session.ResultHandler;
import com.code.session.RowBounds;
import com.code.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 简单执行器
 *
 * @author HeXin
 * @date 2024/01/30
 */
public class SimpleExecutor extends BaseExecutor {
    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    protected <E> List<E> doQuery(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        Statement statement = null;
        try {
            Configuration configuration = mappedStatement.getConfiguration();
            // 新建一个 StatementHandler
            StatementHandler handler = configuration.newStatementHandler(wrapper, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
            // 准备语句
            statement = prepareStatement(handler);
            // 返回结果
            return handler.query(statement, resultHandler);
        } catch (SQLException e) {
            throw new RuntimeException("执行查询操作时发生异常："+e.getMessage());
        } finally {
            closeStatement(statement);
        }
    }

    @Override
    protected int doUpdate(MappedStatement mappedStatement, Object parameter) throws SQLException {
        Statement statement = null;
        try {
            Configuration configuration = mappedStatement.getConfiguration();
            // 新建 StatementHandler
            StatementHandler handler = configuration.newStatementHandler(this, mappedStatement, parameter, RowBounds.DEFAULT, null, null);
            // 准备语句
            statement = prepareStatement(handler);
            // 执行更新操作
            return handler.update(statement);
        } finally {
            closeStatement(statement);
        }
    }

    /**
     * 准备语句
     *
     * @param handler 处理器
     * @return {@link Statement}
     * @throws SQLException sqlexception异常
     */
    private Statement prepareStatement(StatementHandler handler) throws SQLException {
        Statement statement;
        Connection connection = transaction.getConnection();
        // 准备语句
        statement = handler.prepare(connection);
        handler.parameterize(statement);
        return statement;
    }
}
