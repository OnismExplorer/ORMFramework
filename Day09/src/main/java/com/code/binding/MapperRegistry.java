package com.code.binding;

import cn.hutool.core.lang.ClassScanner;
import com.code.session.Configuration;
import com.code.session.SqlSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 映射器注册器
 *
 * @author HeXin
 * @date 2024/01/21
 */
public class MapperRegistry {

    private Configuration configuration;

    public MapperRegistry(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 已有映射器
     */
    private final Map<Class<?>,MapperProxyFactory<?>>
            knownMappers = new HashMap<>();

    /**
     * 获取映射器
     *
     * @param type       类型
     * @param sqlSession SQL 会话
     * @return {@link T}
     */
    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, SqlSession sqlSession){
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if(mapperProxyFactory == null){
            throw new RuntimeException("类型 " + type + " 的MapperRegistry未知.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new RuntimeException("获取Mapper映射器实例时出错。原因：" + e, e);
        }
    }

    /**
     * 添加映射器
     *
     * @param type 类型
     */
    public <T> void addMapper(Class<T> type){

//     Mapper 必须是接口才能被注册
        if(type.isInterface()){
            if(hasMapper(type)){
                // 若已经添加过，则报错重复添加
                throw new RuntimeException("类型 " + type + " 的MapperRegistry已存在.");
            }
            // 注册映射代理工厂
            knownMappers.put(type, new MapperProxyFactory<>(type));
        }
    }

    /**
     * 是否含有映射器
     *
     * @param type 类型
     * @return boolean
     */
    public <T> boolean hasMapper(Class<T> type){
        return knownMappers.containsKey(type);
    }

    public void addMappers(String packageName){
        // 解析包路径下的所有接口，并添加到映射器注册表中
        Set<Class<?>> mapperSet = ClassScanner.scanPackage(packageName);
        for(Class<?> mapperClass : mapperSet){
            addMapper(mapperClass);
        }
    }
}
