package com.code.plugin;

import java.util.Properties;

/**
 * 拦截器接口
 *
 * @author HeXin
 * @date 2024/02/21
 */
public interface Interceptor {

    /**
     * 拦截(插件使用方实现)
     *
     * @param invocation 调用
     * @return {@link Object}
     * @throws Throwable throwable
     */
    Object intercept(Invocation invocation) throws Throwable;

    /**
     * 插件(代理)
     *
     * @param target 目标
     * @return {@link Object}
     */
    default Object plugin(Object target) {
        return Plugin.wrap(target,this);
    }

    /**
     * 设置属性
     *
     * @param properties 属性
     */
    default void setProperties(Properties properties) {

    }
}
