package com.code.binding;

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
    private Map<String, String> sqlSession;

    public MapperProxy(Map<String, String> sqlSession, Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        } else {
            System.out.println(mapperInterface.getName()+"的"+method.getName()+"方法被调用");
            // 执行代理逻辑
            return sqlSession.get(mapperInterface.getName()+"."+method.getName());
        }
    }
}
