package com.code.reflection;

import com.code.reflection.factory.ObjectFactory;
import com.code.reflection.property.PropertyTokenizer;
import com.code.reflection.wrapper.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 元对象
 *
 * @author HeXin
 * @date 2024/02/02
 */
public class MetaObject {

    /**
     * 原始对象
     */
    private Object originalObject;

    /**
     * 对象包装器
     */
    private ObjectWrapper objectWrapper;

    /**
     * 对象工厂
     */
    private ObjectFactory objectFactory;

    /**
     * 对象包装工厂
     */
    private ObjectWrapperFactory objectWrapperFactory;

    @SuppressWarnings("unchecked")
    public MetaObject(Object originalObject, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory) {
        this.originalObject = originalObject;
        this.objectFactory = objectFactory;
        this.objectWrapperFactory = objectWrapperFactory;

        if(originalObject instanceof ObjectWrapper object) {
            // 若对象本身已是 ObjectWrapper 型，则直接复制给objectWrapper
            this.objectWrapper = object;
        } else if(objectWrapperFactory.hasWrapperFor(originalObject)) {
            // 若有包装器，则直接调用ObjectWrapperFactory.getWrapper
            this.objectWrapper = objectWrapperFactory.getWrapperFor(this,originalObject);
        } else if(originalObject instanceof Map) {
            // 若是 Map 集合类型，则直接返回 MapWrapper
            this.objectWrapper = new MapWrapper(this,(Map) originalObject);
        } else if(originalObject instanceof Collection) {
            // 若为 Collection 型，则返回 CollectionWrapper
            this.objectWrapper = new CollectionWrapper(this,(Collection) originalObject);
        } else {
            // 其他则返回 BeanWrapper
            this.objectWrapper = new BeanWrapper(this,originalObject);
        }
    }

    /**
     * 对于对象
     *
     * @param object               对象
     * @param objectFactory        对象工厂
     * @param objectWrapperFactory 对象包装器工厂
     * @return {@link MetaObject}
     */
    public static MetaObject forObject(Object object,ObjectFactory objectFactory,ObjectWrapperFactory objectWrapperFactory) {
        if(object == null) {
            // 处理(包装)null
            return SystemMetaObject.NULL_META_OBJECT;
        }
        return new MetaObject(object,objectFactory,objectWrapperFactory);
    }

    public Object getOriginalObject() {
        return originalObject;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    /* --------以下方法是委派给 ObjectWrapper------ */

    /**
     * 查找属性
     *
     * @param propertyName        属性名称
     * @param useCamelCaseMapping 使用驼峰大小写映射
     * @return {@link String}
     */
    public String findProperty(String propertyName,boolean useCamelCaseMapping){
        return objectWrapper.findProperty(propertyName,useCamelCaseMapping);
    }

    /**
     * 获取 GETTER 名称
     *
     * @return {@link String[]}
     */
    public String[] getGetterNames() {
        return  objectWrapper.getGetterNames();
    }

    /**
     * 获取 setter 名称
     *
     * @return {@link String[]}
     */
    public String[] getSetterNames() {
        return objectWrapper.getSetterNames();
    }

    /**
     * 获取 getter 类型
     *
     * @param name 名字
     * @return {@link Class}<{@link ?}>
     */
    public Class<?> getGetterType(String name) {
        return objectWrapper.getGetterType(name);
    }

    /**
     * 获取 setter 类型
     *
     * @param name 名字
     * @return {@link Class}<{@link ?}>
     */
    public Class<?> getSetterType(String name) {
        return objectWrapper.getSetterType(name);
    }

    /**
     * 是否有 setter
     *
     * @param name 名字
     * @return boolean
     */
    public boolean hasSetter(String name) {
        return objectWrapper.hasSetter(name);
    }

    /**
     * 是否含有 getter
     *
     * @param name 名字
     * @return boolean
     */
    public boolean hasGetter(String name) {
        return objectWrapper.hasGetter(name);
    }

    /**
     * 获取值
     *
     * @param name 名字
     * @return {@link Object}
     */
    public Object getValue(String name) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        if(tokenizer.hasNext()) {
            MetaObject metaValue = metaObjectForProperty(tokenizer.getIndexName());
            if(metaValue == SystemMetaObject.NULL_META_OBJECT) {
                // 若上层为 null，则直接结束返回 null
                return null;
            }
            // 否则递归调用getValue，继续看下一层
            return metaValue.getValue(tokenizer.getChildren());
        }
        return objectWrapper.get(tokenizer);
    }

    /**
     * 设定值
     *
     * @param name  名字
     * @param value 价值
     */
    public void setValue(String name,Object value) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        if(tokenizer.hasNext()) {
            MetaObject metaValue = metaObjectForProperty(tokenizer.getIndexName());
            if(metaValue == SystemMetaObject.NULL_META_OBJECT) {
                if(value == null && tokenizer.getChildren() != null){
                    // 若上层为 null，则需要确认是否有下一层，没有则结束
                    return ;
                } else {
                    // 否则需要 new 一个，委派给 ObjectWrapper.instantiatePropertyValue
                    metaValue = objectWrapper.instantiatePropertyValue(name,tokenizer,objectFactory);
                }
            }
            // 递归调用 setValue
            metaValue.setValue(tokenizer.getChildren(),value);
        } else {
            // 最后一层则委派给 ObjectWrapper.set
            objectWrapper.set(tokenizer,value);
        }
    }

    /**
     * 为属性生成元对象
     *
     * @param name 名字
     * @return {@link MetaObject}
     */
    public MetaObject metaObjectForProperty(String name) {
        // 递归调用
        Object value = getValue(name);
        return MetaObject.forObject(value,objectFactory,objectWrapperFactory);
    }

    public ObjectWrapper getObjectWrapper() {
        return objectWrapper;
    }

    /**
     * 是否为集合
     *
     * @return boolean
     */
    public boolean isCollection() {
        return objectWrapper.isCollection();
    }

    /**
     * 添加属性
     *
     * @param element 元素
     */
    public void add(Object element) {
        objectWrapper.add(element);
    }

    /**
     * 属性全部添加
     *
     * @param list 列表
     */
    public <E> void addAll(List<E> list) {
        objectWrapper.addAll(list);
    }
}
