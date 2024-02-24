package com.code.cache;

import com.code.cache.decorator.TransactionalCache;

import java.util.HashMap;
import java.util.Map;

/**
 * 事务缓存管理器
 *
 * @author HeXin
 * @date 2024/02/24
 */
public class TransactionalCacheManager {

    private Map<Cache, TransactionalCache> transactionalCacheMap = new HashMap<>();

    public void clear(Cache cache) {
        getTransactionalCache(cache).clear();
    }

    public Object get(Cache cache,CacheKey key) {
        return getTransactionalCache(cache).get(key);
    }

    public void put(Cache cache,CacheKey key,Object value) {
        getTransactionalCache(cache).put(key,value);
    }

    /**
     * 提交(全部提交)
     */
    public void commit() {
        for (TransactionalCache value : transactionalCacheMap.values()) {
            value.commit();
        }
    }

    /**
     * 回滚(全部回滚)
     */
    public void rollback() {
        for (TransactionalCache value : transactionalCacheMap.values()) {
            value.rollback();
        }
    }
    /**
     * 获取事务性缓存
     *
     * @param cache 缓存
     * @return {@link TransactionalCache}
     */
    private TransactionalCache getTransactionalCache(Cache cache) {
        TransactionalCache transactionalCache = transactionalCacheMap.get(cache);
        if(transactionalCache == null) {
            transactionalCache = new TransactionalCache(cache);
            transactionalCacheMap.put(cache,transactionalCache);
        }
        return transactionalCache;
    }
}
