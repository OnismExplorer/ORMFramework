package com.code.dao;

public interface StudentDao {
    /**
     * 保存
     */
    void save();

    /**
     * 通过用户 ID 获取名称
     *
     * @param uid uid
     * @return {@link String}
     */
    String getNameByUid(Long uid);

    /**
     * 按 ID 获取编号
     *
     * @param id 编号
     * @return {@link String}
     */
    String getNumberById(Long id);
}
