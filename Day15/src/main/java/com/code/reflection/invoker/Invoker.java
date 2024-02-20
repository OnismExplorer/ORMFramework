package com.code.reflection.invoker;

/**
 * 调用程序
 * 调用(者)程序
 *
 * @author HeXin
 * @date 2024/02/02
 */
public interface Invoker {
    /**
     * 调用
     *
     * @param target 目标
     * @param args   参数
     * @return {@link Object}
     * @throws Exception 例外
     */
    Object invoke(Object target,Object[] args) throws Exception;

    /**
     * 获取类型
     *
     * @return {@link Class}<{@link ?}>
     */
    Class<?> getType();
}
