package com.code.executor.statement;

import com.code.executor.Executor;
import com.code.executor.parameter.ParameterHandler;
import com.code.executor.resultset.ResultSetHandler;
import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.session.Configuration;
import com.code.session.ResultHandler;
import com.code.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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

    protected final ParameterHandler parameterHandler;

    protected final RowBounds rowBounds;

    protected BoundSql boundSql;


    BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject,RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.parameterObject = parameterObject;
        if(boundSql == null) {
            // 因为更新操作不会传入 boundSql 参数，所以这里需要进行初始化操作
            boundSql = mappedStatement.getBoundSql(parameterObject);
        }
        this.boundSql = boundSql;
        this.rowBounds = rowBounds;
        this.resultSetHandler = configuration.newResultSetHandler(executor,mappedStatement,rowBounds,resultHandler,boundSql);
        this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject,boundSql);
    }

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
            throw new RuntimeException("准备 SQL 语句时发生错误："+e.getMessage());
        }
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
