package com.code.cache.Impl;

import com.alibaba.fastjson.JSON;
import com.code.cache.Cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 一级缓存
 *
 * @author HeXin
 * @date 2024/02/22
 */
public class PerpetualCache implements Cache {

    private String id;

    private Map<Object, Object> cache = new HashMap<>();

    public PerpetualCache(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void put(Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object get(Object key) {
        Object object = cache.get(key);
        if (object != null) {
            System.out.println("一级缓存命中 ===> key：" + key + " \tvalue："+ JSON.toJSONString(object));
        }
        return object;
    }

    @Override
    public Object remove(Object key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int size() {
        return cache.size();
    }
}
