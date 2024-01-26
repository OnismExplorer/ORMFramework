package com.code.session;

import com.code.binding.MapperRegistry;
import com.code.datasource.druid.DruidDataSourceFactory;
import com.code.mapping.Environment;
import com.code.mapping.MappedStatement;
import com.code.transaction.jdbc.JdbcTrasactionFactory;
import com.code.type.TypeAliasRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置类
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class Configuration {

    /**
     * 环境类
     */
    protected Environment environment;

    /**
     * 映射器注册器
     */
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * 将映射语句存入在 Map 中
     */
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTrasactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
    }

    /**
     * 扫描注入映射器
     *
     * @param packageName 软件包名称
     */
    public void addMappers(String packageName){
        mapperRegistry.addMappers(packageName);
    }

    /**
     * 添加映射器
     *
     * @param type 类型
     */
    public <T> void addMapper(Class<T> type){
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type,SqlSession sqlSession){
        return mapperRegistry.getMapper(type,sqlSession);
    }

    public boolean hasMapper(Class<?> type){
        return mapperRegistry.hasMapper(type);
    }

    public void addMappedStatement(MappedStatement statement){
        mappedStatements.put(statement.getId(),statement);
    }

    public MappedStatement getMappedStatement(String id){
        return mappedStatements.get(id);
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
