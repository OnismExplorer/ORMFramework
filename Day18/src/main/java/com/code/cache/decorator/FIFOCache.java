package com.code.cache.decorator;

import com.code.cache.Cache;

import java.util.Deque;
import java.util.LinkedList;


/**
 *
 * 先进先出(FIFO)缓存
 * @author HeXin
 * @date 2024/02/23
 */
public class FIFOCache implements Cache {

    /**
     * 委托
     */
    private final Cache delegate;

    /**
     * 键列表
     */
    private Deque<Object> keyList;

    private  int size;

    public FIFOCache(Cache delegate) {
        this.delegate = delegate;
        this.keyList = new LinkedList<>();
        this.size = 1024;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void put(Object key, Object value) {
        cycleKeyList(key);
        delegate.put(key,value);
    }

    @Override
    public Object get(Object key) {
        return delegate.get(key);
    }

    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void clear() {
        delegate.clear();
        keyList.clear();
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    /**
     * 循环处理键表
     *
     * @param key 键
     */
    private void cycleKeyList(Object key) {
        keyList.addLast(key);
        if(keyList.size() > size) {
            Object oldestKey = keyList.removeFirst();
            delegate.remove(oldestKey);
        }
    }
}
