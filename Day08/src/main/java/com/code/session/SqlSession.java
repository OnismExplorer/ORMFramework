package com.code.session;

/**
 * SQL 会话接口
 *
 * @author HeXin
 * @date 2024/01/21
 */
public interface  SqlSession {
    /**
     * 根据指定的SqlID获取一条记录的封装对象
     *
     * @param statement sqlID
     * @return {@link T} 封装之后的对象类型
     */
    <T> T selectOne(String statement);

    /**
     * 根据指定的SqlID获取一条记录的封装对象，该方法允许为sql传递参数
     *
     * @param statement 陈述
     * @param parameter 参数
     * @return {@link T}
     */
    <T> T selectOne(String statement,Object parameter);

    /**
     * 获取映射器(使用泛型，使得类型安全)
     *
     * @param type 类型
     * @return {@link T}
     */
    <T> T getMapper(Class<T> type);

    /**
     * 获取配置类
     *
     * @return {@link Configuration}
     */
    Configuration getConfiguration();
}
