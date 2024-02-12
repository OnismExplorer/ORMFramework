package com.code.executor.result;

import com.code.session.ResultContext;

/**
 * 默认结果上下文
 *
 * @author HeXin
 * @date 2024/02/12
 */
public class DefaultResultContext implements ResultContext {

    /**
     * 结果对象
     */
    private Object resultObject;

    /**
     * 结果统计数
     */
    private int resultCount;

    public DefaultResultContext() {
        this.resultObject = null;
        this.resultCount = 0;
    }

    @Override
    public Object getResultObject() {
        return resultObject;
    }

    @Override
    public int getResultCount() {
        return resultCount;
    }

    /**
     * 下一个结果对象
     *
     * @param resultObject 结果对象
     */
    public void nextResultObject(Object resultObject) {
        resultCount++;
        this.resultObject = resultObject;
    }
}
