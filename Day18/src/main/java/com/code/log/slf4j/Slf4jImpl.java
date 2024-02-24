package com.code.log.slf4j;

import com.code.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * slf4j log 设计模式类
 *
 * @author HeXin
 * @date 2024/02/23
 */
public class Slf4jImpl implements Log {

    public Slf4jImpl(String clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void error(String message, Throwable e) {

    }

    @Override
    public void error(String message) {

    }

    @Override
    public void debug(String message) {

    }

    @Override
    public void trace(String message) {

    }

    @Override
    public void warn(String message) {

    }
}
