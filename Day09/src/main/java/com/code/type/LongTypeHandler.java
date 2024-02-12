package com.code.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Long 类型处理器
 *
 * @author HeXin
 * @date 2024/02/11
 */
public class LongTypeHandler extends BaseTypeHandler<Long>{
    @Override
    protected void setNonNullParameter(PreparedStatement preparedStatement, int i, Long parameter, JdbcType jdbcType) throws SQLException {
        preparedStatement.setLong(i,parameter);
    }
}
