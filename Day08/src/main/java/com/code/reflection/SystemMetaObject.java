package com.code.reflection;

import com.code.reflection.factory.DefaultObjectFactory;
import com.code.reflection.factory.ObjectFactory;
import com.code.reflection.wrapper.DefaultObjectWrapperFactory;
import com.code.reflection.wrapper.ObjectWrapperFactory;

/**
 * 系统元对象
 *
 * @author HeXin
 * @date 2024/02/02
 */
public class SystemMetaObject {

    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();

    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

    public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(NullObject.class,DEFAULT_OBJECT_FACTORY,DEFAULT_OBJECT_WRAPPER_FACTORY);

    private SystemMetaObject(){
        //防止静态类的实例化
    }

    /**
     * 空对象
     *
     * @author HeXin
     * @date 2024/02/02
     */
    private static class NullObject {

    }

    public static MetaObject forObject(Object object) {
        return MetaObject.forObject(object,DEFAULT_OBJECT_FACTORY,DEFAULT_OBJECT_WRAPPER_FACTORY);
    }
}
