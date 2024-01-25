package com.code.dao;

public interface UserDao {

    /**
     * 按 ID 获取用户名
     *
     * @param uid uid
     * @return {@link String}
     */
    String  getNameById(Long uid);

    /**
     * 按 ID 获取用户年龄
     *
     * @param uid uid
     * @return {@link Integer}
     */
    Integer getAgeById(Long uid);
}
