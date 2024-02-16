package com.code.type;

import java.sql.*;

/**
 * 日期类型处理器
 *
 * @author HeXin
 * @date 2024/02/16
 */
public class DateTypeHandler extends BaseTypeHandler<java.sql.Date>{

    @Override
    protected void setNonNullParameter(PreparedStatement preparedStatement, int i, Date parameter, JdbcType jdbcType) throws SQLException {
        preparedStatement.setDate(i,parameter);
    }

    @Override
    protected Date getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getDate(columnName);
    }
}
