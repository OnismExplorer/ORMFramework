package com.code.plugin;

/**
 * 方法签名
 *
 * @author HeXin
 * @date 2024/02/21
 */
public @interface Signature {

    /**
     * 被拦截类
     *
     * @return {@link Class}<{@link ?}>
     */
    Class<?> type();

    /**
     * 被拦截方法
     *
     * @return {@link String}
     */
    String method();

    /**
     * 被拦截类中方法参数
     *
     * @return {@link Class}<{@link ?}>{@link []}
     */
    Class<?>[] args();
}
