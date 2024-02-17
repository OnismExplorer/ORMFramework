package com.code.reflection.invoker;

import java.lang.reflect.Method;

/**
 * 方法调用程序
 *
 * @author HeXin
 * @date 2024/02/02
 */
public class MethodInvoker implements Invoker{

    private Class<?> type;

    private Method method;

    public MethodInvoker(Method method) {
        this.method = method;

        // 若只有一个参数，则直接返回参数类型，否则返回 return 类型
        if(method.getParameterTypes().length == 1) {
            type = method.getParameterTypes()[0];
        }
        type = method.getReturnType();
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return method.invoke(target, args);
    }

    @Override
    public Class<?> getType() {
        return type;
    }
}
