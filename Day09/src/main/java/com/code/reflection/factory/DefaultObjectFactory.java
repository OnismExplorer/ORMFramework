package com.code.reflection.factory;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * 默认对象工厂(所有的对象都由工厂来生产)
 *
 * @author HeXin
 * @date 2024/02/02
 */
public class DefaultObjectFactory implements ObjectFactory, Serializable {

    private static final long serialVersionUID = -8992992802928826378L;


    @Override
    public void setProperties(Properties properties) {
        // 默认没有属性设置
    }

    @Override
    public <T> T create(Class<T> type) {
        return create(type,null,null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        // 解析接口
        Class<?> classToCreate = resolveInterface(type);
        // 类实例化
        return (T) instantiateClass(classToCreate,constructorArgTypes,constructorArgs);
    }

    /**
     * 使用给定的构造函数参数实例化指定类型的对象。
     *
     * @param <T>               对象的类型参数
     * @param type              要实例化的类的类型
     * @param constructorArgTypes 构造函数参数的类型列表，可以为null
     * @param constructorArgs    构造函数参数的值列表，可以为null
     * @return 实例化后的对象
     * @throws RuntimeException 如果实例化失败，则抛出运行时异常
     */
    private <T> T instantiateClass(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        try {
            Constructor<T> constructor;

            // 如果未传入构造函数参数类型和值，则调用空构造函数
            if (constructorArgTypes == null || constructorArgs == null) {
                constructor = type.getDeclaredConstructor();
                // 如果构造函数不可访问，则设置为可访问
                if (!constructor.canAccess(constructor)) {
                    constructor.setAccessible(true);
                }
                // 调用构造函数并返回实例化对象
                return constructor.newInstance();
            }

            // 如果传入构造函数参数类型和值，则调用指定构造函数
            constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[0]));
            // 如果构造函数不可访问，则设置为可访问
            if (!constructor.canAccess(constructor)) {
                constructor.setAccessible(true);
            }
            // 调用构造函数并返回实例化对象
            return constructor.newInstance(constructorArgs.toArray(new Object[0]));

        } catch (Exception e) {
            // 如果实例化失败，捕获异常并包装成运行时异常重新抛出

            // 构建构造函数参数类型的字符串表示
            StringBuilder argTypes = new StringBuilder();
            if (constructorArgTypes != null) {
                for (Class<?> argType : constructorArgTypes) {
                    argTypes.append(argType.getSimpleName());
                    argTypes.append(",");
                }
            }

            // 构建构造函数参数值的字符串表示
            StringBuilder argValues = new StringBuilder();
            if (constructorArgs != null) {
                for (Object argValue : constructorArgs) {
                    argValues.append(argValue);
                    argValues.append(",");
                }
            }

            // 抛出包含详细错误信息的运行时异常
            throw new RuntimeException("使用无效的类型（" + argTypes + "）或值（" + argValues + "）实例化 " + type + " 时发生错误。原因：" + e, e);
        }
    }

    /**
     * 解析接口类型，将其映射到具体的实现类。
     *
     * @param type 待解析的接口类型
     * @return 映射到的具体实现类，如果不是特定接口，则返回原始类型
     */
    protected Class<?> resolveInterface(Class<?> type) {
        Class<?> classToCreate;

        // 根据接口类型映射到具体的实现类
        if (type == List.class || type == Collection.class || type == Iterable.class) {
            // List|Collection|Iterable 映射到 ArrayList
            classToCreate = ArrayList.class;
        } else if (type == Map.class) {
            // Map 映射到 HashMap
            classToCreate = HashMap.class;
        } else if (type == SortedSet.class) {
            // SortedSet 映射到 TreeSet
            classToCreate = TreeSet.class;
        } else if (type == Set.class) {
            // Set 映射到 HashSet
            classToCreate = HashSet.class;
        } else {
            // 如果不是特定接口，则返回原始类型
            classToCreate = type;
        }

        return classToCreate;
    }


    @Override
    public <T> boolean isCollection(Class<T> type) {
        return false;
    }
}
