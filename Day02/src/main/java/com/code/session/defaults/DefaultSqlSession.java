package com.code.session.defaults;

import com.code.binding.MapperRegistry;
import com.code.session.SqlSession;

/**
 * 默认 SQL 会话实现类
 *
 * @author HeXin
 * @date 2024/01/21
 */
public class DefaultSqlSession implements SqlSession {

    /**
     * 映射器注册器
     */
    private MapperRegistry mapperRegistry;

    public DefaultSqlSession(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    @Override
    public <T> T selectOne(String statement) {
        return (T) (statement+"被代理！");
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return (T) ("入参为 "+parameter+" 的 "+statement+" 方法被代理！");
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return mapperRegistry.getMapper(type, this);
    }
}
