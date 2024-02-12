package com.code.dao;

import com.code.entity.User;

public interface UserDao {

    /**
     * 按 ID 获取用户名
     *
     * @param uid uid
     * @return {@link String}
     */
    User getNameById(Long uid);

    /**
     * 按 ID 获取用户年龄
     *
     * @param uid uid
     * @return {@link Integer}
     */
    Integer getAgeById(Long uid);

    /**
     * 获取用户
     *
     * @param user 用户
     * @return {@link User}
     */
    User getUser(User user);

    User getUserById(Long id);
}
