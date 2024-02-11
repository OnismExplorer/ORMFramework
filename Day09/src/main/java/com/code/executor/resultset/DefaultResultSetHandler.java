package com.code.executor.resultset;

import com.code.executor.Executor;
import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认 Map 结果集处理器
 *
 * @author HeXin
 * @date 2024/01/30
 */
public class DefaultResultSetHandler implements ResultSetHandler{

    private final MappedStatement mappedStatement;

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement,BoundSql boundSql) {
        this.mappedStatement = mappedStatement;
    }

    @Override
    public <E> List<E> handleResultSets(Statement statement) throws SQLException {
        ResultSet resultSet = statement.getResultSet();
        return resultSet2Obj(resultSet,mappedStatement.getResultType());
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz) {
        List<T> list = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            // 每次遍历行值
            while (resultSet.next()) {
                T obj = (T) clazz.getDeclaredConstructor().newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);
                    String columnName = metaData.getColumnName(i);
                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    Method method;
                    if (value instanceof Timestamp) {
                        method = clazz.getMethod(setMethod, Date.class);
                    } else {
                        method = clazz.getMethod(setMethod, value.getClass());
                    }
                    method.invoke(obj, value);
                }
                list.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
