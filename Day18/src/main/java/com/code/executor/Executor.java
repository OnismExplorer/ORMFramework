package com.code.executor;

import com.code.cache.CacheKey;
import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.session.ResultHandler;
import com.code.session.RowBounds;
import com.code.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * 执行器(定义标准接口)
 *
 * @author HeXin
 * @date 2024/01/30
 */
public interface Executor {

    /**
     * 无结果处理器
     */
    ResultHandler NO_RESULT_HANDLER = null;

    /**
     * 普通查询(缓存)
     *
     * @param parameter       参数
     * @param resultHandler   结果处理程序
     * @param boundSql        绑定 SQL
     * @param mappedStatement 映射语句
     * @return {@link List}<{@link E}>
     */
    <E> List<E> query(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql);

    /**
     * 查询
     *
     * @param mappedStatement 映射语句
     * @param parameter       参数
     * @param rowBounds       行范围
     * @param resultHandler   结果处理程序
     * @return {@link List}<{@link E}>
     */
    <E> List<E> query(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler);

    /**
     * 更新数据(返回受影响行数)
     *
     * @param mappedStatement 映射语句
     * @param parameter       参数
     * @return int
     * @throws SQLException sqlexception异常
     */
    int update(MappedStatement mappedStatement,Object parameter) throws SQLException;

    /**
     * 获取事务
     *
     * @return {@link Transaction}
     */
    Transaction getTransaction();

    /**
     * 提交事务
     *
     * @param required 是否必须
     * @throws SQLException SQLException
     */
    void commit(boolean required) throws SQLException;

    /**
     * 回滚事务
     *
     * @param required 是否必须
     * @throws SQLException SQLException
     */
    void rollback(boolean required) throws SQLException;

    /**
     * 关闭
     *
     * @param forceRollback 强制回滚
     */
    void close(boolean forceRollback);

    /**
     * 清除 Session 缓存
     */
    void clearLocalCache();

    /**
     * 创建缓存键
     *
     * @param mappedStatement 映射语句
     * @param parameterObject 参数对象
     * @param rowBounds       行范围
     * @param boundSql        绑定sql
     * @return {@link CacheKey}
     */
    CacheKey createCacheKey(MappedStatement mappedStatement,Object parameterObject,RowBounds rowBounds,BoundSql boundSql);

    /**
     * 设置执行器包装
     *
     * @param executor 遗嘱执行人
     */
    void setExecutorWrapper(Executor executor);
}
