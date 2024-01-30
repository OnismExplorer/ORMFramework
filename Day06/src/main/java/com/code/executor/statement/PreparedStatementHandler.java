package com.code.executor.statement;

import com.code.executor.Executor;
import com.code.executor.resultset.ResultSetHandler;
import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.session.Configuration;
import com.code.session.ResultHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 预处理语句处理器
 *
 * @author HeXin
 * @date 2024/01/30
 */
public class PreparedStatementHandler extends BaseStatementHandler{
    public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, ResultHandler resultHandler, BoundSql boundSql) {
        super(executor, mappedStatement, parameterObject, resultHandler, boundSql);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(boundSql.sql());
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) statement;
        preparedStatement.setLong(1,Long.parseLong(((Object[]) parameterObject)[0].toString()));
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) statement;
        preparedStatement.execute();
        return resultSetHandler.<E>handleResultSets(preparedStatement);
    }
}
