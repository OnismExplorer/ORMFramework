package com.code.reflection.wrapper;

import com.code.reflection.MetaObject;
import com.code.reflection.SystemMetaObject;
import com.code.reflection.factory.ObjectFactory;
import com.code.reflection.property.PropertyTokenizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map 包装类
 * @author HeXin
 * @date 2024/02/03
 */
public class MapWrapper extends BaseWrapper {

    /**
     * 原对象
     */
    private Map<String,Object> map;

    public MapWrapper(MetaObject metaObject, Map<String, Object> map) {
        super(metaObject);
        this.map = map;
    }

    @Override
    public Object get(PropertyTokenizer tokenizer) {
        if(tokenizer.getIndex() != null) {
            Object collection = resolveCollection(tokenizer, map);
            return getCollectionValue(tokenizer,collection);
        }
        return map.get(tokenizer.getName());
    }

    @Override
    public void set(PropertyTokenizer tokenizer, Object value) {
        if(tokenizer.getIndex() != null) {
            Object collection = resolveCollection(tokenizer, map);
            setCollectionValue(tokenizer,collection,value);
        }
        map.put(tokenizer.getName(),value);
    }

    @Override
    public String findProperty(String name, boolean useCamelCaseMapping) {
        return name;
    }

    @Override
    public String[] getGetterNames() {
        return map.keySet().toArray(new String[0]);
    }

    @Override
    public String[] getSetterNames() {
        return map.keySet().toArray(new String[0]);
    }

    @Override
    public Class<?> getSetterType(String name) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        if (tokenizer.hasNext()) {
            MetaObject metaValue = metaObject.metaObjectForProperty(tokenizer.getIndexName());
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                return Object.class;
            } else {
                return metaValue.getSetterType(tokenizer.getChildren());
            }
        } else {
            if (map.get(name) != null) {
                return map.get(name).getClass();
            } else {
                return Object.class;
            }
        }

    }

    @Override
    public Class<?> getGetterType(String name) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        if (tokenizer.hasNext()) {
            MetaObject metaValue = metaObject.metaObjectForProperty(tokenizer.getIndexName());
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                return Object.class;
            } else {
                return metaValue.getGetterType(tokenizer.getChildren());
            }
        } else {
            if (map.get(name) != null) {
                return map.get(name).getClass();
            } else {
                return Object.class;
            }
        }

    }

    @Override
    public boolean hasSetter(String name) {
        return true;
    }

    @Override
    public boolean hasGetter(String name) {
        PropertyTokenizer tokenizer = new PropertyTokenizer(name);
        if (tokenizer.hasNext()) {
            if (map.containsKey(tokenizer.getIndexName())) {
                MetaObject metaValue = metaObject.metaObjectForProperty(tokenizer.getIndexName());
                if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                    return true;
                } else {
                    return metaValue.hasGetter(tokenizer.getChildren());
                }
            } else {
                return false;
            }
        } else {
            return map.containsKey(tokenizer.getName());
        }

    }

    @Override
    public MetaObject instantiatePropertyValue(String name, PropertyTokenizer tokenizer, ObjectFactory objectFactory) {
        HashMap<String, Object> hashMap = new HashMap<>();
        set(tokenizer,hashMap);
        return MetaObject.forObject(hashMap, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory());
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
