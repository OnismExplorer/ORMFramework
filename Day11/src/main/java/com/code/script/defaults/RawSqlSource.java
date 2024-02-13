package com.code.script.defaults;

import com.code.builder.SqlSourceBuilder;
import com.code.mapping.BoundSql;
import com.code.mapping.SqlSource;
import com.code.script.xmltags.DynamicContext;
import com.code.script.xmltags.SqlNode;
import com.code.session.Configuration;

import java.util.HashMap;

public class RawSqlSource implements SqlSource {

    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration,getSql(configuration,rootSqlNode),parameterType);
    }

    public RawSqlSource(Configuration configuration,String sql,Class<?> parameterType) {
        SqlSourceBuilder builder = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = builder.parse(sql,clazz,new HashMap<>());
    }
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

    /**
     * 获得 sql 语句
     *
     * @param configuration 配置
     * @param rootSqlNode   SQL根节点
     * @return {@link String}
     */
    public static String getSql(Configuration configuration,SqlNode rootSqlNode){
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }
}
