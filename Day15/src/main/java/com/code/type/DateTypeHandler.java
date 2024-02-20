package com.code.type;

import java.sql.*;
import java.util.Date;

/**
 * 日期类型处理器
 *
 * @author HeXin
 * @date 2024/02/16
 */
public class DateTypeHandler extends BaseTypeHandler<Date>{


    @Override
    protected void setNonNullParameter(PreparedStatement preparedStatement, int i, Date parameter, JdbcType jdbcType) throws SQLException {
        preparedStatement.setTimestamp(i, new Timestamp((parameter).getTime()));
    }

    @Override
    protected Date getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp sqlTimestamp = resultSet.getTimestamp(columnName);
        if (sqlTimestamp != null) {
            return new Date(sqlTimestamp.getTime());
        }
        return null;
    }

    @Override
    public Date getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        Timestamp sqlTimestamp = resultSet.getTimestamp(columnIndex);
        if (sqlTimestamp != null) {
            return new Date(sqlTimestamp.getTime());
        }
        return null;
    }
}
