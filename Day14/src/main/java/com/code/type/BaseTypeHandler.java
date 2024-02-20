package com.code.type;

import com.code.session.Configuration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 类型处理器基类
 *
 * @author HeXin
 * @date 2024/02/12
 */
public abstract class BaseTypeHandler<T> implements TypeHandler<T>{

    protected Configuration configuration;

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, T parameter, JdbcType jdbcType) throws SQLException {
        // 定义抽象方法，由其子类实现不同类型的属性设置
        setNonNullParameter(preparedStatement,i,parameter,jdbcType);
    }

    @Override
    public T getResult(ResultSet resultSet, String columnName) throws SQLException {
        return getNullableResult(resultSet,columnName);
    }

    /**
     * 设置非空参数
     *
     * @param preparedStatement 事先准备好声明中
     * @param i                 计数器
     * @param parameter         参数
     * @param jdbcType          jdbc类型
     * @throws SQLException sqlexception异常
     */
    protected abstract void setNonNullParameter(PreparedStatement preparedStatement,int i,T parameter,JdbcType jdbcType) throws SQLException;
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 获取可空结果
     *
     * @param resultSet  结果集
     * @param columnName 列名
     * @return {@link T}
     * @throws SQLException sqlexception异常
     */
    protected abstract T getNullableResult(ResultSet resultSet, String columnName) throws SQLException;

    @Override
    public T getResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return getNullableResult(resultSet,columnIndex);
    }

    /**
     * 获取可空结果
     *
     * @param resultSet   结果集
     * @param columnIndex 列索引
     * @return {@link T}
     * @throws SQLException sqlexception异常
     */
    public abstract T getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException;
}
