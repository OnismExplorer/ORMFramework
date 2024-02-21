package com.code.script.xmltags;

import com.code.builder.SqlSourceBuilder;
import com.code.mapping.BoundSql;
import com.code.mapping.SqlSource;
import com.code.session.Configuration;

import java.util.Map;

/**
 * 动态SQL源码
 *
 * @author HeXin
 * @date 2024/02/20
 */
public class DynamicSqlSource implements SqlSource {

    /**
     * 配置
     */
    private Configuration configuration;

    /**
     * sql节点
     */
    private SqlNode sqlNode;

    public DynamicSqlSource(Configuration configuration, SqlNode sqlNode) {
        this.configuration = configuration;
        this.sqlNode = sqlNode;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // 生成一个 DynamicContext 动态上下文
        DynamicContext context = new DynamicContext(configuration, parameterObject);
        // SqlNode.apply 将 ${} 参数替换，并不替换 #{} 参数
         sqlNode.apply(context);

         // 调用 SqlSourceBuilder
        SqlSourceBuilder builder = new SqlSourceBuilder(configuration);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();

        // SqlSourceBuilder.parse 返回的是 StaticSqlSource，解析过程将这项参数都替换成？，即最基本的 JDBC 的 SQL 语句
        SqlSource sqlSource = builder.parse(context.getSql(), parameterType, context.getBindings());

        // SqlSource.getBoundSql，非递归调用，而是调用 StaticSqlSource 实现类
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        for(Map.Entry<String,Object> entry : context.getBindings().entrySet()) {
            boundSql.setAdditionalParameters(entry.getKey(),entry.getValue());
        }
        return boundSql;
    }
}
