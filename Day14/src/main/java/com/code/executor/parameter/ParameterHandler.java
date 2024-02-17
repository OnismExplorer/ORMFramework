package com.code.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 参数处理器
 *
 * @author HeXin
 * @date 2024/02/11
 */
public interface ParameterHandler {

    /**
     * 获取参数对象
     *
     * @return {@link Object}
     */
    Object getParameterObject();

    /**
     * 设置参数
     *
     * @param preparedStatement 事先准备好声明中
     * @throws SQLException sqlexception异常
     */
    void setParameters(PreparedStatement preparedStatement) throws SQLException;
}
