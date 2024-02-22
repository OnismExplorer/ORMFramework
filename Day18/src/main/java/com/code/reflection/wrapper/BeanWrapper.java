package com.code.reflection.wrapper;

import com.code.reflection.MetaClass;
import com.code.reflection.MetaObject;
import com.code.reflection.SystemMetaObject;
import com.code.reflection.factory.ObjectFactory;
import com.code.reflection.invoker.Invoker;
import com.code.reflection.property.PropertyTokenizer;

import java.util.List;

/**
 * Bean 包装器
 * @author HeXin
 * @date 2024/02/02
 */
public class BeanWrapper extends BaseWrapper{

    /**
     * 原对象
     */
    private Object object;

    /**
     * 元类
     */
    private MetaClass metaClass;

    public BeanWrapper(MetaObject metaObject,Object object) {
        super(metaObject);
        this.object = object;
        this.metaClass = MetaClass.forClass(object.getClass());
    }

    @Override
    public Object get(PropertyTokenizer tokenizer) {
        // 若有index，则证明是集合，需要解析集合，调用 BaseWrapper.resolveCollection 和 getCollectionValue
        if(tokenizer.getIndex() != null) {
            Object collection = resolveCollection(tokenizer, object);
            return getCollectionValue(tokenizer,collection);
        }
        // 否则调用 getBeanProperty
        return getBeanProperty(tokenizer,object);
    }

    /**
     * 获取 Bean 属性
     * @param tokenizer 分词器
     * @param object    对象
     * @return {@link Object}
     */
    private Object getBeanProperty(PropertyTokenizer tokenizer,Object object) {
        try {
            // 获取 getter 方法，调用
            Invoker method = metaClass.getGetInvoker(tokenizer.getName());
            return method.invoke(object,NO_ARGUMENTS);
        } catch (RuntimeException e){
            throw e;
        } catch (Throwable th){
            throw new RuntimeException("无法从 " + object.getClass() + " 获取属性 '" + tokenizer.getName() + "'。原因：" + th.toString(), th);
        }
    }

    @Override
    public void set(PropertyTokenizer tokenizer, Object value) {
// 若有index，则证明是集合，需要解析集合，调用 BaseWrapper.resolveCollection 和 setCollectionValue
        if(tokenizer.getIndex() != null) {
            Object collection = resolveCollection(tokenizer, object);
            setCollectionValue(tokenizer,collection,value);
        }
        // 否则调用 getBeanProperty
        setBeanProperty(tokenizer,object,value);
    }

    private void setBeanProperty(PropertyTokenizer tokenizer,Object object,Object value) {
        try {
            Invoker method = metaClass.getSetInvoker(tokenizer.getName());
            Object[] params = {value};
            method.invoke(object,params);
        } catch (Throwable th){
            throw new RuntimeException("无法设置 '" + object.getClass() + "' 的属性 '" + tokenizer.getName() + "' 为值 '" + value + "'。原因：" + th.toString(), th);

        }
    }
    @Override
    public String findProperty(String name, boolean useCamelCaseMapping) {
        return metaClass.findProperty(name,useCamelCaseMapping);
    }

    @Override
    public String[] getGetterNames() {
        return metaClass.getGetterNames();
    }

    @Override
    public String[] getSetterNames() {
        return metaClass.getSetterNames();
    }

    @Override
    public Class<?> getSetterType(String name) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        if(tokenizer.hasNext()){
            MetaObject metaValue = metaObject.metaObjectForProperty(tokenizer.getIndexName());
            if(metaValue == SystemMetaObject.NULL_META_OBJECT) {
                return metaClass.getSetterType(name);
            }
            return metaValue.getSetterType(tokenizer.getChildren());
        }
        return metaClass.getSetterType(name);
    }

    @Override
    public Class<?> getGetterType(String name) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        if(tokenizer.hasNext()) {
            MetaObject metaValue = metaObject.metaObjectForProperty(tokenizer.getIndexName());
            if(metaValue == SystemMetaObject.NULL_META_OBJECT) {
                return metaClass.getGetterType(name);
            }
            return metaValue.getGetterType(tokenizer.getChildren());
        }
        return metaClass.getGetterType(name);
    }

    @Override
    public boolean hasSetter(String name) {
        // 使用 PropertyTokenizer 对属性名进行解析
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        if (tokenizer.hasNext()) {
            // 如果属性名有子属性，递归检查子属性的 setter 方法
            if (metaClass.hasSetter(tokenizer.getIndexName())) {
                // 获取子属性的 MetaObject
                MetaObject metaValue = metaObject.metaObjectForProperty(tokenizer.getIndexName());
                // 如果子属性的 MetaObject 为 NULL_META_OBJECT，说明属性不存在
                if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                    // 检查整个属性名对应的 setter 方法
                    return metaClass.hasSetter(name);
                }
                // 递归检查子属性的 setter 方法
                return metaValue.hasSetter(tokenizer.getChildren());
            }
            // 如果属性名的第一部分不存在对应的 setter 方法，返回 false
            return false;
        }
        // 如果属性名只有一个部分，检查整个属性名对应的 setter 方法
        return metaClass.hasSetter(name);
    }

    @Override
    public boolean hasGetter(String name) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        if(tokenizer.hasNext()) {
            if(metaClass.hasGetter(tokenizer.getIndexName())) {
                MetaObject metaValue = metaObject.metaObjectForProperty(tokenizer.getIndexName());
                if(metaValue == SystemMetaObject.NULL_META_OBJECT) {
                    return metaClass.hasSetter(name);
                }
                return metaValue.hasGetter(tokenizer.getIndexName());
            }
            return false;
        }
        return metaClass.hasGetter(name);
    }

    @Override
    public MetaObject instantiatePropertyValue(String name, PropertyTokenizer tokenizer, ObjectFactory objectFactory) {
        MetaObject metaValue;

        // 获取属性的类型
        Class<?> type = getSetterType(tokenizer.getName());

        try {
            // 使用对象工厂创建新的属性值对象
            Object newObject = objectFactory.create(type);

            // 创建属性值的 MetaObject
            metaValue = MetaObject.forObject(newObject, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory());
            // 设置新创建的属性值到原对象中
            set(tokenizer, newObject);
        } catch (Exception e) {
            // 如果无法实例化属性值，抛出运行时异常
            throw new RuntimeException("无法设置属性值 '" + name + "'，因为 '" + name + "' 为 null，且无法在 " + type.getName() + " 实例上实例化。原因：" + e.toString(), e);
        }
        // 返回新属性值的 MetaObject
        return metaValue;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public void add(Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <E> void addAll(List<E> element) {
        throw new UnsupportedOperationException();
    }
}
