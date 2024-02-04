package com.code.reflection.invoker;

import java.lang.reflect.Field;

/**
 * setter 字段调用程序
 *
 * @author HeXin
 * @date 2024/02/02
 */
public class SetFieldInvoker implements Invoker{

    private Field field;

    public SetFieldInvoker(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        field.set(target,args[0]);
        return null;
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
