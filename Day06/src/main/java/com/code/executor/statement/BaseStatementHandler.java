package com.code.executor.statement;

import com.code.executor.Executor;
import com.code.executor.resultset.ResultSetHandler;
import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.session.Configuration;
import com.code.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 语句处理器的抽象基类
 *
 * @author HeXin
 * @date 2024/01/30
 */
public abstract class BaseStatementHandler implements StatementHandler{

    protected Configuration configuration;

    protected final Executor executor;

    protected final MappedStatement mappedStatement;

    protected final Object parameterObject;

    protected final ResultSetHandler resultSetHandler;

    public BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, ResultHandler resultHandler, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.parameterObject = parameterObject;
        this.resultSetHandler = configuration.newResultSetHandler(executor,mappedStatement,boundSql);
        this.boundSql = boundSql;
    }

    protected BoundSql boundSql;
    @Override
    public Statement prepare(Connection connection) throws SQLException {
        Statement statement = null;
        try {
            // 实例化 Statement
            statement = instantiateStatement(connection);
            // 参数设置(可以将其抽取出来提供配置)
            statement.setQueryTimeout(350);
            statement.setFetchSize(10000);
            return statement;
        } catch (Exception e){
            System.err.println("准备 SQL 语句时发生错误："+e.getMessage());
        }
        return null;
    }

    /**
     * 实例化语句
     *
     * @param connection 连接
     * @return {@link Statement}
     * @throws SQLException SQLException
     */
    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;
}
