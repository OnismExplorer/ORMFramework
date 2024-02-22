package com.code.executor.keygen;

import com.code.executor.Executor;
import com.code.mapping.MappedStatement;

import java.sql.Statement;

/**
 * 键值生成器接口
 *
 * @author HeXin
 * @date 2024/02/18
 */
public interface KeyGenerator {
    /**
     * 对于顺序(非自增)主键而言，在执行插入语句前必须为语句指定一个主键值
     *
     * @param executor        执行器
     * @param mappedStatement 映射语句
     * @param statement       语句
     * @param parameter       参数
     */
    void processBefore(Executor executor, MappedStatement mappedStatement, Statement statement,Object parameter);

    /**
     * 对于自增主键，在插入数据时不需要主键，而是在插入过程中自动获取一个自增主键
     *
     * @param executor        执行器
     * @param mappedStatement 映射语句
     * @param statement       语句
     * @param parameter       参数
     */
    void processAfter(Executor executor, MappedStatement mappedStatement, Statement statement,Object parameter);
}
