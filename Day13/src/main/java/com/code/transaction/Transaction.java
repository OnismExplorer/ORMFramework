package com.code.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务接口
 * @author HeXin
 * @date 2024/01/26
 */
public interface Transaction {
    /**
     * 获取连接
     *
     * @return {@link Connection}
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;

    /**
     * 提交事务
     * @throws SQLException SQLException
     */
    void commit() throws SQLException;

    /**
     * 回滚
     *
     * @throws SQLException SQLException
     */
    void rollback() throws SQLException;

    /**
     * 关闭事务
     *
     * @throws SQLException SQLException
     */
    void close() throws SQLException;
}
