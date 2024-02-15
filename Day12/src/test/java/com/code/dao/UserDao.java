package com.code.dao;

import com.code.annotations.*;
import com.code.entity.User;

import java.util.List;

public interface UserDao {

    /**
     * 按 ID 获取用户名
     *
     * @param uid uid
     * @return {@link String}
     */
    @Select("SELECT id, username, password, avatar\n" +
            "        FROM user\n" +
            "        where id = #{id}")
    User getNameById(Long uid);


    /**
     * 通过id获取用户
     *
     * @param id id
     * @return {@link User}
     */
    @Select("SELECT id,username,password,avatar FROM user where id = #{id}")
    User getUserById(Long id);

    /**
     * 获取用户列表
     *
     * @return {@link List}<{@link User}>
     */
    @Select("SELECT id,username,password,avatar\n" +
            "        FROM user;")
    List<User> getList();

    /**
     * 保存用户
     *
     * @param user 用户
     */
    @Insert("INSERT INTO user\n" +
            "        (id,username,password,avatar)\n" +
            "        VALUES (#{id},#{username},#{password},#{avatar});")
    void save(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户
     */
    @Update("UPDATE user\n" +
            "        SET username = #{username}\n" +
            "        WHERE id = #{id};")
    void updateInfo(User user);

    /**
     * 删除用户
     *
     * @param id id
     */
    @Delete("DELETE FROM user WHERE id = #{id};")
    void delete(String id);
}
