package com.code.binding;

import com.code.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<Method,MapperMethod> methodCache = new ConcurrentHashMap<>();

    public Map<Method, MapperMethod> getMethodCache() {
        return methodCache;
    }

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    /**
     * 新建实例
     *
     * @param sqlSession SQL 会话
     * @return {@link T}
     */
    @SuppressWarnings("unchecked")
    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession,mapperInterface,methodCache);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(),new Class[]{mapperInterface}, mapperProxy);
    }
}
