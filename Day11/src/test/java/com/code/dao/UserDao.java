package com.code.dao;

import com.code.entity.User;

import java.util.List;

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

    /**
     * 通过id获取用户
     *
     * @param id id
     * @return {@link User}
     */
    User getUserById(Long id);

    /**
     * 获取用户列表
     *
     * @return {@link List}<{@link User}>
     */
    List<User> getList();

    /**
     * 保存用户
     *
     * @param user 用户
     */
    void save(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户
     */
    void updateInfo(User user);

    /**
     * 删除用户
     *
     * @param id id
     */
    void delete(String id);
}
