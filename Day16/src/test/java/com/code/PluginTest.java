package com.code;

import com.code.executor.statement.StatementHandler;
import com.code.mapping.BoundSql;
import com.code.plugin.Interceptor;
import com.code.plugin.Intercepts;
import com.code.plugin.Invocation;
import com.code.plugin.Signature;

import java.sql.Connection;
import java.util.Properties;

/**
 * 插件测试
 *
 * @author HeXin
 * @date 2024/02/21
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class})})
public class PluginTest implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取StatementHandler
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        // 获取SQL信息
        BoundSql boundSql = statementHandler.getBoundsql();
        String sql = boundSql.getSql();
        // 输出SQL
        System.out.println("拦截到 SQL：" + sql);
        // 放行
        return invocation.proceed();

    }

    @Override
    public void setProperties(Properties properties) {
        System.out.println("输出参数："+properties.getProperty("test1"));
        System.out.println("输出参数："+properties.getProperty("test2"));
    }
}
