package com.code.executor.statement;

import com.code.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 语句处理器
 *
 * @author HeXin
 * @date 2024/01/30
 */
public interface StatementHandler {

    /**
     * 准备 SQL 语句
     *
     * @param connection 连接
     * @return {@link Statement}
     * @throws SQLException SQLException
     */
    Statement prepare(Connection connection) throws SQLException;

    /**
     * 参数化
     *
     * @param statement 陈述
     * @throws SQLException SQLException
     */
    void parameterize(Statement statement) throws SQLException;

    /**
     * 执行查询
     *
     * @param statement     陈述
     * @param resultHandler 结果处理程序
     * @return {@link List}<{@link E}>
     * @throws SQLException SQLException
     */
    <E>List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;
}
