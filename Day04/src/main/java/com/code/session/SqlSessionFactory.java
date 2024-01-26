package com.code.session;

/**
 * SQL 会话工厂
 *
 * @author HeXin
 * @date 2024/01/21
 */
public interface SqlSessionFactory {
    /**
     * 公开会话
     *
     * @return {@link SqlSession}
     */
    SqlSession openSession();
}
