package com.code.transaction.jdbc;

import com.code.session.TransactionIsolationLevel;
import com.code.transaction.Transaction;
import com.code.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * JDBC Trasaction 工厂
 *
 * @author HeXin
 * @date 2024/01/26
 */
public class JdbcTrasactionFactory implements TransactionFactory {
    @Override
    public Transaction newTrasaction(Connection connection) {
        return new JdbcTrasaction(connection);
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new JdbcTrasaction(dataSource,level,autoCommit);
    }
}
