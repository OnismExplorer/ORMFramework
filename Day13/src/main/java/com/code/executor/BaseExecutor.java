package com.code.executor;

import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.session.Configuration;
import com.code.session.ResultHandler;
import com.code.session.RowBounds;
import com.code.transaction.Transaction;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 执行器抽象基类
 *
 * @author HeXin
 * @date 2024/01/30
 */
public abstract class BaseExecutor implements Executor{

    /**
     * 配置
     */
    protected Configuration configuration;

    /**
     * 交易
     */
    protected Transaction transaction;

    /**
     * wrapper映射
     */
    protected Executor wrapper;

    protected boolean closed;

    protected BaseExecutor(Configuration configuration, Transaction transaction) {
        this.configuration = configuration;
        this.transaction = transaction;
        this.wrapper = this;
    }

    @Override
    public <E> List<E> query(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql){
        if(closed){
            throw new RuntimeException("执行器为关闭状态！");
        }
        return doQuery(mappedStatement,parameter,rowBounds,resultHandler,boundSql);
    }

    @Override
    public int update(MappedStatement mappedStatement, Object parameter) throws SQLException {
        return doUpdate(mappedStatement,parameter);
    }

    /**
     * 执行查询
     *
     * @param mappedStatement 映射语句
     * @param parameter       参数
     * @param resultHandler   结果处理程序
     * @param boundSql        绑定 SQL
     * @return {@link List}<{@link E}>
     */
    protected abstract <E> List<E> doQuery(MappedStatement mappedStatement, Object parameter,RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql);

    /**
     * 执行更新
     *
     * @param mappedStatement 映射语句
     * @param parameter       参数
     * @return int
     * @throws SQLException sqlexception异常
     */
    protected abstract int doUpdate(MappedStatement mappedStatement,Object parameter) throws SQLException;
    @Override
    public Transaction getTransaction() {
        if(closed){
            throw new RuntimeException("执行器为关闭状态！");
        }
        return transaction;
    }

    @Override
    public void commit(boolean required) throws SQLException {
        if(closed){
            throw new RuntimeException("执行器为关闭状态！");
        }
        if(required){
            transaction.commit();
        }
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        if(closed){
            throw new RuntimeException("执行器为关闭状态！");
        }
        if(required){
            transaction.rollback();
        }
    }

    @Override
    public void close(boolean forceRollback) {
        try{
            try{
                rollback(forceRollback);
            } finally {
                transaction.close();
            }
        }catch (SQLException e){
            System.err.println("关闭执行器时发生异常："+e.getMessage());
        } finally {
            transaction = null;
            closed = true;
        }
    }

    protected void closeStatement(Statement statement) {
        if(statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println("关闭 statement 时发生异常：" + e);
            }
        }
    }
}
