package com.code.executor;

import com.code.executor.statement.StatementHandler;
import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.session.Configuration;
import com.code.session.ResultHandler;
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
public class SimpleExecutor extends BaseExecutor{
    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    protected <E> List<E> doQuery(MappedStatement mappedStatement, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
        try{
            Configuration configuration = mappedStatement.getConfiguration();
            StatementHandler handler = configuration.newStatementHandler(this,mappedStatement,parameter,resultHandler,boundSql);
            Connection connection = transaction.getConnection();
            Statement statement = handler.prepare(connection);
            handler.parameterize(statement);
            return handler.query(statement,resultHandler);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}
