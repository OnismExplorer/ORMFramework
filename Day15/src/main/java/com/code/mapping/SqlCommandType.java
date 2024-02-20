package com.code.mapping;

/**
 * SQL命令类型
 *
 * @author HeXin
 * @date 2024/02/17
 */
public enum SqlCommandType {
    /**
      * 未知
      */
    UNKNOWN,
    /**
      * 插入(新增)
      */
    INSERT,
    /**
      * 删除
      */
    DELETE,
    /**
      * 更新
      */
    UPDATE,

    /**
      * 查询
      */
    SELECT;

}
