package com.code.reflection.invoker;

import java.lang.reflect.Field;

/**
 * getter 字段调用程序
 *
 * @author HeXin
 * @date 2024/02/02
 */
public class GetFieldInvoker implements Invoker{

    private Field field;

    public GetFieldInvoker(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return field.get(target);
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
