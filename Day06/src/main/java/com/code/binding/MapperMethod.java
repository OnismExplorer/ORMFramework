package com.code.binding;

import com.code.mapping.MappedStatement;
import com.code.mapping.SqlCommandType;
import com.code.session.Configuration;
import com.code.session.SqlSession;

import java.lang.reflect.Method;


/**
 * Mapper 映射方法
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class MapperMethod {
    private final SqlCommand command;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration){
        this.command = new SqlCommand(configuration,mapperInterface,method);
    }

    /**
     * 执行方法
     *
     * @param sqlSession SQL 会话
     * @param args       参数
     * @return {@link Object}
     */
    public Object execute(SqlSession sqlSession,Object[] args){
        Object result = null;
        switch (command.getType()) {
            case INSERT:
                break;
            case DELETE:
                break;
            case UPDATE:
                break;
            case SELECT: result = sqlSession.selectOne(command.getName(),args);
                break;
            default:
                throw new RuntimeException("未知执行方法：" + command.getName());
        }
        return result;
    }

    /**
     * sql 命令
     *
     * @author HeXin
     * @date 2024/01/25
     */
    public static class SqlCommand{
        private final SqlCommandType type;
        private final String name;

        public SqlCommand(Configuration configuration,Class<?> mapperInterface,Method method){
            String statementName = mapperInterface.getName() + "." + method.getName();
            MappedStatement statement = configuration.getMappedStatement(statementName);
            name = statement.getId();
            type = statement.getSqlCommandType();
        }

        public SqlCommandType getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }
}
