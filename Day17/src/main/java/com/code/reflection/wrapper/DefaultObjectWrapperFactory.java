package com.code.reflection.wrapper;

import com.code.reflection.MetaObject;

/**
 * 默认对象包装器工厂
 *
 * @author HeXin
 * @date 2024/02/02
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory{
    @Override
    public boolean hasWrapperFor(Object object) {
        return false;
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        throw new RuntimeException("永远不应该调用DefaultObjectWrapperFactory来提供ObjectWrapper。");
    }
}
