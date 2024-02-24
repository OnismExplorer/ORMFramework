package com.code.cache.decorator;

import com.code.cache.Cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 事务缓存
 *
 * @author HeXin
 * @date 2024/02/24
 */
public class TransactionalCache implements Cache {

    private Cache delegate;

    /**
     * 提交时是否清除缓存
     */
    private boolean clearOnCommit;

    /**
     * 提交时要添加元素
     */
    private Map<Object,Object> entriesToAddOnCommit;

    /**
     * 缓存中缺少元素
     */
    private Set<Object> entriesMissedInCache;

    public TransactionalCache(Cache delegate) {
        this.delegate = delegate;
        // 默认提交不清除缓存
        this.clearOnCommit = false;
        this.entriesMissedInCache = new HashSet<>();
        this.entriesToAddOnCommit = new HashMap<>();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void put(Object key, Object value) {
        entriesToAddOnCommit.put(key, value);
    }

    @Override
    public Object get(Object key) {
        Object object = delegate.get(key);
        if(object == null) {
            entriesMissedInCache.add(key);
        }
        return clearOnCommit ? null : object;
    }

    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void clear() {
        clearOnCommit = true;
        entriesToAddOnCommit.clear();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    public void commit() {
        if(clearOnCommit) {
            delegate.clear();
        }
        flushPendingEntries();
        reset();
    }

    public void reset() {
        clearOnCommit = false;
        entriesToAddOnCommit.clear();
        entriesMissedInCache.clear();
    }

    public void rollback() {
        unlockMissedEntries();
        reset();
    }

    /**
     * 解锁错过条目
     */
    private void unlockMissedEntries() {
        for (Object entry : entriesMissedInCache) {
            delegate.put(entry,null);
        }
    }

    /**
     * 刷新数据到 MappedStatement的Cache中，即将数据填充至 Mapper XML 级别下
     */
    private void flushPendingEntries() {
        for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
            delegate.put(entry.getKey(), entry.getValue());
        }
        for (Object entry : entriesMissedInCache) {
            if (!entriesToAddOnCommit.containsKey(entry)) {
                delegate.put(entry, null);
            }
        }
    }
}
