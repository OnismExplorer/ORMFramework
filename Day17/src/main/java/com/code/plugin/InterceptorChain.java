package com.code.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 拦截器链
 *
 * @author HeXin
 * @date 2024/02/21
 */
public class InterceptorChain {

    private final List<Interceptor> interceptors = new ArrayList<>();

    /**
     * 配置所有插件
     *
     * @param target 目标
     * @return {@link Object}
     */
    public Object pluginAll(Object target) {
        for (Interceptor interceptor : interceptors) {
            target = interceptor.plugin(target);
        }
        return target;
    }

    /**
     * 添加拦截器
     *
     * @param interceptor 拦截器
     */
    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    /**
     * 获取拦截器集合
     *
     * @return {@link List}<{@link Interceptor}>
     */
    public List<Interceptor> getInterceptors() {
        // 防止常量集合被修改
        return Collections.unmodifiableList(interceptors);
    }
}
