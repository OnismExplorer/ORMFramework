package com.code.session.defaults;

import com.code.executor.Executor;
import com.code.mapping.Environment;
import com.code.session.Configuration;
import com.code.session.SqlSession;
import com.code.session.SqlSessionFactory;
import com.code.session.TransactionIsolationLevel;
import com.code.transaction.Transaction;
import com.code.transaction.TransactionFactory;

import java.sql.SQLException;

/**
 * 默认 SQL 会话工厂，用于获取DefaultSqlSession
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {
    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configurationn) {
        this.configuration = configurationn;
    }

    @Override
    public SqlSession openSession() {
        Transaction transaction = null;
        try {
            final Environment environment = configuration.getEnvironment();
            TransactionFactory transactionFactory = environment.transactionFactory();
            transaction = transactionFactory.newTransaction(configuration.getEnvironment().dataSource(), TransactionIsolationLevel.READ_COMMITTED,false);
            // 创建执行器
            final Executor executor = configuration.newExecutor(transaction);
            // 创建 DefaultSqlSession
            return new DefaultSqlSession(configuration,executor);
        } catch (Exception e){
            try {
                assert transaction != null;
                transaction.close();
            } catch (SQLException ex){
                System.err.println("关闭事务时发生错误："+ex.getMessage());
            }
            throw new RuntimeException("开启会话失败："+e.getMessage());
        }
    }
}
