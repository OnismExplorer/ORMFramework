package com.code.builder;

import com.code.mapping.BoundSql;
import com.code.mapping.ParameterMapping;
import com.code.mapping.SqlSource;
import com.code.session.Configuration;

import java.util.List;

/**
 * 静态SQL源码
 *
 * @author HeXin
 * @date 2024/02/05
 */
public class StaticSqlSource implements SqlSource {

    /**
     * SQL 语句
     */
    private String sql;

    /**
     * 参数映射
     */
    private List<ParameterMapping> parameterMappings;

    /**
     * 配置
     */
    private Configuration configuration;



    public StaticSqlSource(Configuration configuration,String sql) {
        this(sql,null,configuration);
    }
    public StaticSqlSource(String sql, List<ParameterMapping> parameterMappings, Configuration configuration) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration,sql,parameterMappings,parameterObject);
    }
}
