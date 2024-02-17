package com.code.reflection.factory;

import java.util.List;
import java.util.Properties;

/**
 * 对象工厂接口
 *
 * @author HeXin
 * @date 2024/02/02
 */
public interface ObjectFactory {

    /**
     * 设置属性
     *
     * @param properties 属性
     */
    void setProperties(Properties properties);

    /**
     * 创造对象
     *
     * @param type 类型
     * @return {@link T}
     */
    <T> T create(Class<T> type);

    /**
     * 创造对象(使用指定的构造函数和构造函数参数)
     *
     * @param type                类型
     * @param constructorArgTypes 构造函数 arg 类型
     * @param constructorArgs     构造函数 args
     * @return {@link T}
     */
    <T> T create(Class<T> type, List<Class<?>> constructorArgTypes,List<Object> constructorArgs);

    /**
     * 是否为集合
     *
     * @param type 类型
     * @return boolean
     */
    <T> boolean isCollection(Class<T> type);
}
