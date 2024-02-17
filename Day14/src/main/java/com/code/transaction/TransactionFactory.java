package com.code.transaction;

import com.code.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 事务工厂
 *
 * @author HeXin
 * @date 2024/01/26
 */
public interface TransactionFactory {

    /**
     * 根据数据库连接创建事务
     * @param connection 数据库连接
     * @return {@link Transaction}
     */
    Transaction newTrasaction(Connection connection);


    /**
     * 根据数据源与事务隔离级别创建事务
     * @param dataSource 数据源
     * @param level      水平
     * @param autoCommit 自动提交
     * @return {@link Transaction}
     */
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level,boolean autoCommit);
}
