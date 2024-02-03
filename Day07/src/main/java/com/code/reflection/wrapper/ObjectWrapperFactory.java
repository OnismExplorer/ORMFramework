package com.code.reflection.wrapper;

import com.code.reflection.MetaObject;

/**
 * 对象包装器工厂
 *
 * @author HeXin
 * @date 2024/02/02
 */
public interface ObjectWrapperFactory {

    /**
     * 判断是否有包装器
     * @param object 对象
     * @return boolean
     */
    boolean hasWrapperFor(Object object);

    /**
     * 获取包装器
     *
     * @param metaObject 元对象
     * @param object     对象
     * @return {@link ObjectWrapper}
     */
    ObjectWrapper getWrapperFor(MetaObject metaObject,Object object);
}
