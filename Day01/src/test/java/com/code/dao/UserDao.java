package com.code.dao;

public interface UserDao {
    // 定义与数据库交互的增删改查等操作

    /**
     * 按 ID 获取名称
     *
     * @param uid uid
     * @return {@link String}
     */
    String  getNameById(Long uid);

    /**
     * 按 ID 获取年龄
     *
     * @param uid uid
     * @return {@link Integer}
     */
    Integer getAgeById(Long uid);
}
