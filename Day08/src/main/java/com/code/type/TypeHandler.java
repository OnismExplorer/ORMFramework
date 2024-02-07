package com.code.type;

import java.sql.PreparedStatement;
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
}
