package com.code.reflection.wrapper;

import com.code.reflection.MetaObject;
import com.code.reflection.factory.ObjectFactory;
import com.code.reflection.property.PropertyTokenizer;

import java.util.Collection;
import java.util.List;

/**
 * 集合包装器
 *
 * @author HeXin
 * @date 2024/02/02
 */
public class CollectionWrapper implements ObjectWrapper{

    /**
     * 原对象
     */
    private Collection<Object> object;

    public CollectionWrapper(MetaObject metaObject,Collection<Object> object) {
        this.object = object;
    }


    /**
     * get 不允许，只能添加元素
     *
     * @param tokenizer 分词器
     * @return {@link Object}
     */
    @Override
    public Object get(PropertyTokenizer tokenizer) {
        throw new UnsupportedOperationException();
    }

    /**
     * set 不允许，只能添加元素
     *
     * @param tokenizer 分词器
     * @param value     价值
     */
    @Override
    public void set(PropertyTokenizer tokenizer, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String findProperty(String name, boolean useCamelCaseMapping) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getGetterNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getSetterNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> getSetterType(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> getGetterType(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasSetter(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasGetter(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MetaObject instantiatePropertyValue(String name, PropertyTokenizer tokenizer, ObjectFactory objectFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public void add(Object element) {
        object.add(element);
    }

    @Override
    public <E> void addAll(List<E> element) {
        object.addAll(element);
    }
}
