package com.code.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 类型处理器
 *
 * @author HeXin
 * @date 2024/02/07
 */
public interface TypeHandler<T> {

    /**
     * 设置参数
     *
     * @param preparedStatement 事先准备好声明中
     * @param i                 我
     * @param parameter         参数
     * @param jdbcType          jdbc类型
     * @throws SQLException
     */
    void setParameter(PreparedStatement preparedStatement,int i,T parameter,JdbcType jdbcType) throws SQLException;

    /**
     * 获取结果
     *
     * @param resultSet  结果集
     * @param columnName 列名
     * @return {@link T}
     * @throws SQLException sqlexception异常
     */
    T getResult(ResultSet resultSet,String columnName) throws SQLException;

    /**
     * 获取结果
     *
     * @param resultSet   结果集
     * @param columnIndex 列索引
     * @return {@link T}
     * @throws SQLException sqlexception异常
     */
    T getResult(ResultSet resultSet,int columnIndex) throws SQLException;
}
