package com.code.reflection.wrapper;

import com.code.reflection.MetaObject;
import com.code.reflection.factory.ObjectFactory;
import com.code.reflection.property.PropertyTokenizer;

import java.util.List;

/**
 * 对象包装器
 *
 * @author HeXin
 * @date 2024/02/02
 */
public interface ObjectWrapper {

    /**
     * 获取
     *
     * @param tokenizer 分词器
     * @return {@link Object}
     */
    Object get(PropertyTokenizer tokenizer);

    /**
     * 设置
     *
     * @param tokenizer 分词器
     * @param value     价值
     */
    void set(PropertyTokenizer tokenizer,Object value);

    /**
     * 查找属性
     *
     * @param name                名字
     * @param useCamelCaseMapping 使用驼峰大小写映射
     * @return {@link String}
     */
    String findProperty(String name,boolean useCamelCaseMapping);

    /**
     * 获取 GETTER 名称列表
     *
     * @return {@link String[]}
     */
    String[] getGetterNames();

    /**
     * 获取 setter 名称列表
     *
     * @return {@link String[]}
     */
    String[] getSetterNames();

    /**
     * 获取 setter 类型
     *
     * @param name 名字
     * @return {@link Class}<{@link ?}>
     */
    Class<?> getSetterType(String name);

    /**
     * 获取 getter 类型
     *
     * @param name 名字
     * @return {@link Class}<{@link ?}>
     */
    Class<?> getGetterType(String name);

    /**
     * 是否有指定的 setter
     *
     * @param name 名字
     * @return boolean
     */
    boolean hasSetter(String name);

    /**
     * 是否有指定的 getter
     *
     * @param name 名字
     * @return boolean
     */
    boolean hasGetter(String name);

    /**
     * 实例化属性
     * @param name          名字
     * @param tokenizer     分词器
     * @param objectFactory 对象工厂
     * @return {@link MetaObject}
     */
    MetaObject instantiatePropertyValue(String name, PropertyTokenizer tokenizer, ObjectFactory objectFactory);

    /**
     * 是否为集合
     *
     * @return boolean
     */
    boolean isCollection();

    /**
     * 添加属性
     *
     * @param element 元素
     */
    void add(Object element);

    /**
     * 属性全部添加
     *
     * @param element 元素
     */
    <E> void addAll(List<E> element);
}
