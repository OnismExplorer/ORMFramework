package com.code.executor;

import com.alibaba.fastjson.JSON;
import com.code.cache.Cache;
import com.code.cache.CacheKey;
import com.code.cache.TransactionalCacheManager;
import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.session.ResultHandler;
import com.code.session.RowBounds;
import com.code.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * 二级缓存执行器
 *
 * @author HeXin
 * @date 2024/02/24
 */
public class CacheExecutor implements Executor{

    private Logger logger = LoggerFactory.getLogger(CacheExecutor.class);
    /**
     * 委托
     */
    private  Executor delegate;

    private TransactionalCacheManager transactionalCacheManager = new TransactionalCacheManager();
    public CacheExecutor(Executor delegate) {
        this.delegate = delegate;
        delegate.setExecutorWrapper(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> List<E> query(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) {
        Cache cache = mappedStatement.getCache();
        if (cache != null) {
            flushCacheIfRequired(mappedStatement);
            if (mappedStatement.isUseCache() && resultHandler == null) {
                List<E> list = (List<E>) transactionalCacheManager.get(cache, key);
                if (list == null) {
                    list = delegate.<E>query(mappedStatement, parameter, rowBounds, resultHandler, key, boundSql);
                    // cache：缓存队列实现类，FIFO
                    // key：哈希值 [mappedStatementId + offset + limit + SQL + queryParams + environment]
                    // list：查询的数据
                    transactionalCacheManager.put(cache, key, list);
                }
                // 打印调试日志，记录二级缓存获取数据
                if (cache.size() > 0 && logger.isDebugEnabled()) {
                    logger.debug("二级缓存：{}", JSON.toJSONString(list));
                    System.out.println("二级缓存命中 ===>"+ JSON.toJSONString(list));
                }
                return list;
            }
        }
        return delegate.<E>query(mappedStatement, parameter, rowBounds, resultHandler, key, boundSql);

    }

    /**
     * 如果需要，刷新缓存
     *
     * @param mappedStatement 映射语句
     */
    private void flushCacheIfRequired(MappedStatement mappedStatement) {
        Cache cache = mappedStatement.getCache();
        if(cache != null && mappedStatement.isFlushCacheRequired()) {
            transactionalCacheManager.clear(cache);
        }
    }

    @Override
    public <E> List<E> query(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) {
        // 获取绑定SQL
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        // 创建缓存Key
        CacheKey key = createCacheKey(mappedStatement, parameter, rowBounds, boundSql);
        return query(mappedStatement, parameter, rowBounds, resultHandler, key, boundSql);

    }

    @Override
    public int update(MappedStatement mappedStatement, Object parameter) throws SQLException {
        return delegate.update(mappedStatement,parameter);
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public void commit(boolean required) throws SQLException {
        delegate.commit(required);
        transactionalCacheManager.commit();
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        try {
            delegate.rollback(required);
        } finally {
            if (required) {
                transactionalCacheManager.rollback();
            }
        }

    }

    @Override
    public void close(boolean forceRollback) {
        try {
            if (forceRollback) {
                transactionalCacheManager.rollback();
            } else {
                transactionalCacheManager.commit();
            }
        } finally {
            delegate.close(forceRollback);
        }

    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    @Override
    public CacheKey createCacheKey(MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        return delegate.createCacheKey(mappedStatement,parameterObject,rowBounds,boundSql);
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        throw new UnsupportedOperationException("此方法无法调用！");
    }
}
