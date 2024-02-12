package com.code.executor.resultset;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 结果集处理器
 *
 * @author HeXin
 * @date 2024/01/30
 */
public interface ResultSetHandler {
    /**
     * 处理结果集
     *
     * @param statement 陈述
     * @return {@link List}<{@link E}>
     * @throws SQLException SQLException
     */
    <E>List<E> handleResultSets(Statement statement) throws SQLException;
}
