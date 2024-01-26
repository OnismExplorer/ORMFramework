package com.code.binding;

import com.code.session.SqlSession;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 映射器
 *
 * @author HeXin
 * @date 2024/01/20
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    @Serial
    private static final long serialVersionUID = -642454035851357637L;
    private final Class<T> mapperInterface;
    /**
     * SQL 会话参数
     */
    private SqlSession sqlSession;

    /**
     * 方法缓存
     */
    private final Map<Method,MapperMethod> methodCache;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface,Map<Method,MapperMethod> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    /**
     * 调用
     *
     * @param proxy  代理
     * @param method 方法
     * @param args   参数
     * @return {@link Object}
     * @throws Throwable 可投掷
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        } else {
            final MapperMethod mapperMethod = cacheMapperMethod(method);
            return mapperMethod.execute(sqlSession,args);
        }
    }

    /**
     * 缓存中查找 Mapper 映射器方法
     *
     * @param method 方法
     * @return {@link MapperMethod}
     */
    private MapperMethod cacheMapperMethod(Method method){
        MapperMethod mapperMethod = methodCache.get(method);
        if(mapperMethod == null){
            // 找不到方法则新建方法
            mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
            // 将其再放入缓存
            methodCache.put(method,mapperMethod);
        }
        return mapperMethod;
    }
}
