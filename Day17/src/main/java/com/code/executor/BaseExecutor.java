package com.code.executor;

import com.code.cache.CacheKey;
import com.code.cache.Impl.PerpetualCache;
import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.mapping.ParameterMapping;
import com.code.reflection.MetaObject;
import com.code.session.Configuration;
import com.code.session.LocalCacheScope;
import com.code.session.ResultHandler;
import com.code.session.RowBounds;
import com.code.transaction.Transaction;
import com.code.type.TypeHandlerRegistry;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 执行器抽象基类
 *
 * @author HeXin
 * @date 2024/01/30
 */
public abstract class BaseExecutor implements Executor {

    /**
     * 配置
     */
    protected Configuration configuration;

    /**
     * 交易
     */
    protected Transaction transaction;

    /**
     * wrapper映射
     */
    protected Executor wrapper;

    protected boolean closed;

    /**
     * 本地缓存
     */
    protected PerpetualCache localCache;

    /**
     * 查询堆栈
     */
    protected int queryStack = 0;

    protected BaseExecutor(Configuration configuration, Transaction transaction) {
        this.configuration = configuration;
        this.transaction = transaction;
        this.wrapper = this;
        this.localCache = new PerpetualCache("LocalCache");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> List<E> query(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) {
        if (closed) {
            throw new RuntimeException("执行器为关闭状态！");
        }
        // 清理局部缓存(查询堆栈为0则进行清理)，避免递归调用清理
        if (queryStack == 0 && mappedStatement.isFlushCacheRequired()) {
            clearLocalCache();
        }
        List<E> list;
        try {
            queryStack++;
            // 根据cacheKey从localCache中查询数据
            list = resultHandler == null ? (List<E>) localCache.get(key) : null;
            if (list == null) {
                list = queryFromDatabase(mappedStatement, parameter, rowBounds, resultHandler, key, boundSql);
            }
        } finally {
            queryStack--;
        }
        if (queryStack == 0 && (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT)) {
                clearLocalCache();

        }
        return list;
    }

    /**
     * 清除本地缓存
     */
    @Override
    public void clearLocalCache() {
        if (!closed) {
            localCache.clear();
        }
    }

    /**
     * 创建缓存键用于缓存查询结果。
     *
     * @param mappedStatement 映射语句对象，包含了映射语句的配置信息
     * @param parameterObject 执行映射语句时使用的参数对象
     * @param rowBounds       分页参数，包含了查询的偏移量和限制条数
     * @param boundSql        绑定的 SQL 对象，包含了SQL语句及其参数映射等信息
     * @return 缓存键，用于唯一标识查询结果的缓存项
     * @throws RuntimeException 如果执行器已关闭，则抛出运行时异常
     */
    @Override
    public CacheKey createCacheKey(MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        // 检查执行器状态，如果已关闭，则抛出异常
        if (closed) {
            throw new RuntimeException("执行器为关闭状态！");
        }

        // 创建缓存键对象
        CacheKey cacheKey = new CacheKey();

        // 更新缓存键，包括映射语句ID、分页参数、绑定的SQL等信息
        cacheKey.update(mappedStatement.getId());
        cacheKey.update(rowBounds.getOffset());
        cacheKey.update(rowBounds.getLimit());
        cacheKey.update(boundSql.getSql());

        // 获取SQL语句的参数映射信息
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // 获取类型处理器注册表
        TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();

        // 遍历参数映射，更新缓存键的值
        for (ParameterMapping parameterMapping : parameterMappings) {
            Object value;
            String propertyName = parameterMapping.getProperty();

            // 检查是否存在额外的参数
            if (boundSql.hasAdditionalParameter(propertyName)) {
                value = boundSql.getAdditionalParameter(propertyName);
            } else if (parameterObject == null) {
                // 如果参数对象为空，则值为null
                value = null;
            } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                // 如果参数对象的类型有对应的类型处理器，则值为参数对象本身
                value = parameterObject;
            } else {
                // 否则，从参数对象中获取属性值
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                value = metaObject.getValue(propertyName);
            }

            // 更新缓存键的值
            cacheKey.update(value);
        }

        // 如果配置中存在环境信息，则也加入到缓存键中
        if (configuration.getEnvironment() != null) {
            cacheKey.update(configuration.getEnvironment().id());
        }

        // 返回最终的缓存键
        return cacheKey;
    }


    @Override
    public <E> List<E> query(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) {
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        // 创建缓存Key
        CacheKey key = createCacheKey(mappedStatement, parameter, rowBounds, boundSql);
        return query(mappedStatement, parameter, rowBounds, resultHandler, key, boundSql);
    }

    @Override
    public int update(MappedStatement mappedStatement, Object parameter) throws SQLException {
        if (closed) {
            throw new RuntimeException("执行器为关闭状态！");
        }
        // 清理缓存
        clearLocalCache();
        return doUpdate(mappedStatement, parameter);
    }

    /**
     * 执行查询
     *
     * @param mappedStatement 映射语句
     * @param parameter       参数
     * @param resultHandler   结果处理程序
     * @param boundSql        绑定 SQL
     * @param rowBounds       行范围
     * @return {@link List}<{@link E}>
     */
    protected abstract <E> List<E> doQuery(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql);

    /**
     * 执行更新
     *
     * @param mappedStatement 映射语句
     * @param parameter       参数
     * @return int
     * @throws SQLException sqlexception异常
     */
    protected abstract int doUpdate(MappedStatement mappedStatement, Object parameter) throws SQLException;

    @Override
    public Transaction getTransaction() {
        if (closed) {
            throw new RuntimeException("执行器为关闭状态！");
        }
        return transaction;
    }

    @Override
    public void commit(boolean required) throws SQLException {
        if (closed) {
            throw new RuntimeException("执行器为关闭状态！");
        }
        clearLocalCache();
        if (required) {
            transaction.commit();
        }
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        if (!closed) {
            try {
                clearLocalCache();
            } finally {
                if (required) {
                    transaction.rollback();
                }
            }
        }
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            try {
                rollback(forceRollback);
            } finally {
                transaction.close();
            }
        } catch (SQLException e) {
            System.err.println("关闭执行器时发生异常：" + e.getMessage());
        } finally {
            transaction = null;
            closed = true;
        }
    }

    /**
     * 关闭语句
     *
     * @param statement 声明
     */
    protected void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println("关闭 statement 时发生异常：" + e);
            }
        }
    }

    /**
     * 从数据库查询
     *
     * @param mappedStatement 映射语句
     * @param parameter       参数
     * @param rowBounds       行范围
     * @param handler         处理程序
     * @param key             关键
     * @param boundSql        绑定sql
     * @return {@link List}<{@link T}>
     */
    private <T> List<T> queryFromDatabase(MappedStatement mappedStatement,Object parameter,RowBounds rowBounds,ResultHandler handler,CacheKey key,BoundSql boundSql) {
        List<T> list;
        localCache.put(key,ExecutionPlaceholder.EXECUTION_PLACEHOLDER);
        try {
            list = doQuery(mappedStatement,parameter,rowBounds,handler,boundSql);
        } finally {
            localCache.remove(key);
        }
        // 存入缓存
        localCache.put(key, list);
        return list;
    }
}
