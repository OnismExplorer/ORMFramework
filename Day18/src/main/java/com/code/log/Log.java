package com.code.log;

/**
 * 日志接口
 *
 * @author HeXin
 * @date 2024/02/23
 */
public interface Log {
    /**
     * 是否启用调试
     *
     * @return boolean
     */
    boolean isDebugEnabled();

    /**
     * 是否启用跟踪
     *
     * @return boolean
     */
    boolean isTraceEnabled();

    /**
     * 错误
     *
     * @param e       e
     * @param message 消息
     */
    void error(String message, Throwable e);

    /**
     * 错误
     *
     * @param message 消息
     */
    void error(String message);

    /**
     * 调试
     *
     * @param message 消息
     */
    void debug(String message);

    /**
     * 跟踪
     *
     * @param message 消息
     */
    void trace(String message);

    /**
     * 警告
     *
     * @param message 消息
     */
    void warn(String message);
}
