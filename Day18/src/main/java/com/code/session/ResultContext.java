package com.code.session;

/**
 * 结果上下文
 *
 * @author HeXin
 * @date 2024/02/12
 */
public interface ResultContext {

    /**
     * 获取结果对象
     *
     * @return {@link Object}
     */
    Object getResultObject();

    /**
     * 获取记录数
     *
     * @return int
     */
    int getResultCount();
}
