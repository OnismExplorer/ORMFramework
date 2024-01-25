package com.code.session;

import com.code.binding.MapperRegistry;
import com.code.mapping.MappedStatement;

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
     * 映射器注册器
     */
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * 将映射语句存入在 Map 中
     */
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

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
}
