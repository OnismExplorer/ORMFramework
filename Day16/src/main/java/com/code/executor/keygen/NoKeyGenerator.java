package com.code.executor.keygen;

import com.code.executor.Executor;
import com.code.mapping.MappedStatement;

import java.sql.Statement;

/**
 * 不使用键值生成器
 *
 * @author HeXin
 * @date 2024/02/18
 */
public class NoKeyGenerator implements KeyGenerator{
    @Override
    public void processBefore(Executor executor, MappedStatement mappedStatement, Statement statement, Object parameter) {
        //
    }

    @Override
    public void processAfter(Executor executor, MappedStatement mappedStatement, Statement statement, Object parameter) {
        //
    }
}
