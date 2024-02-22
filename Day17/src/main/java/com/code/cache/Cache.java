package com.code.cache;

/**
 * 缓存接口(SPI)
 *
 * @author HeXin
 * @date 2024/02/21
 */
public interface Cache {

    /**
     * 获取缓存唯一 ID 标识
     *
     * @return {@link String}
     */
    String getId();

    /**
     * 存入值
     *
     * @param key   键
     * @param value 值
     */
    void put(Object key,Object value);

    /**
     * 获取值
     *
     * @param key 键
     * @return {@link Object}
     */
    Object get(Object key);

    /**
     * 删除值
     *
     * @param key 关键
     * @return {@link Object}
     */
    Object remove(Object key);

    /**
     * 清空缓存
     */
    void clear();

    /**
     * 获取缓存大小
     *
     * @return int
     */
    int size();
}
