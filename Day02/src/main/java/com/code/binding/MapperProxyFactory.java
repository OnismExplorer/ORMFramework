package com.code.binding;

import com.code.session.SqlSession;

import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * 映射器代理工厂
 *
 * @author HeXin
 * @date 2024/01/20
 */
public class MapperProxyFactory<T> {
    /**
     * mapper 接口
     */
    private final Class<T> mapperInterface;

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    /**
     * 新建实例
     *
     * @param sqlSession SQL 会话
     * @return {@link T}
     */
    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession,mapperInterface);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(),new Class[]{mapperInterface}, mapperProxy);
    }
}
