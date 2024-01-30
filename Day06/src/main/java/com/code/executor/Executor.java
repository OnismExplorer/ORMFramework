package com.code.executor;

import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.session.ResultHandler;
import com.code.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * 执行器(定义标准接口)
 *
 * @author HeXin
 * @date 2024/01/30
 */
public interface Executor {

    /**
     * 无结果处理器
     */
    ResultHandler NO_RESULT_HANDLER = null;

    /**
     * 普通查询
     *
     * @param parameter       参数
     * @param resultHandler   结果处理程序
     * @param boundSql        绑定 SQL
     * @param mappedStatement 映射语句
     * @return {@link List}<{@link E}>
     */
    <E> List<E> query(MappedStatement mappedStatement, Object parameter, ResultHandler resultHandler, BoundSql boundSql);

    /**
     * 获取事务
     *
     * @return {@link Transaction}
     */
    Transaction getTransaction();

    /**
     * 提交事务
     *
     * @param required 是否必须
     * @throws SQLException SQLException
     */
    void commit(boolean required) throws SQLException;

    /**
     * 回滚事务
     *
     * @param required 是否必须
     * @throws SQLException SQLException
     */
    void rollback(boolean required) throws SQLException;

    /**
     * 关闭
     *
     * @param forceRollback 强制回滚
     */
    void close(boolean forceRollback);
}
