package com.code.mapping;

import com.code.reflection.MetaObject;
import com.code.session.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 绑定的SQL，从SqlSource而来，将动态内容处理完成得到的SQL语句，其中包括?以及绑定的参数
 *
 * @author HeXin
 * @date 2024/01/26
 */
public class BoundSql {
    /**
     * sql 语句
     */
    private String sql;

    /**
     * 参数映射
     */
    private List<ParameterMapping> parameterMappings;

    /**
     * 参数对象
     */
    private Object parameterObject;

    /**
     * 额外参数
     */
    private final Map<String,Object> additionalParameters;

    /**
     * 元参数
     */
    private MetaObject metaParameters;

    public BoundSql(Configuration configuration,String sql,List<ParameterMapping> parameterMappings,Object parameterObject) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.parameterObject =parameterObject;
        this.additionalParameters = new HashMap<>();
        this.metaParameters = configuration.newMetaObject(additionalParameters);
    }

    public String getSql() {
        return sql;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

    public Object getParameterObject() {
        return parameterObject;
    }

    /**
     * 是否有附加参数
     *
     * @param name 名字。
     * @return boolean
     */
    public boolean hasAdditionalParameter(String name) {
        return metaParameters.hasGetter(name);
    }

    public void setAdditionalParameters(String name,Object value) {
        metaParameters.setValue(name,value);
    }

    public Object getAdditionalParameters(String name) {
        return metaParameters.getValue(name);
    }
}
