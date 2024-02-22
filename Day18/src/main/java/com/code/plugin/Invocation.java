package com.code.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 调用信息
 *
 * @author HeXin
 * @date 2024/02/21
 */
public class Invocation {

    /**
     * 调用对象
     */
    private Object target;

    private Method method;

    private Object[] args;

    public Invocation(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    /**
     * 调用执行(放行)
     *
     * @return {@link Object}
     * @throws InvocationTargetException 调用目标异常
     * @throws IllegalAccessException    非法访问异常
     */
    public Object proceed() throws InvocationTargetException,IllegalAccessException {
        return method.invoke(target,args);
    }
}
