package com.code.type;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;


/**
 * JDBC 类型枚举
 * @author HeXin
 * @date 2024/01/26
 */
public enum JdbcType {

    /**
     * 整数
     */
    INTEGER(Types.INTEGER),
    BIGINT(Types.BIGINT),
    /**
     * 浮
     */
    FLOAT(Types.FLOAT),
    /**
     * 双
     */
    DOUBLE(Types.DOUBLE),
    /**
     * 精确整数
     */
    DECIMAL(Types.DECIMAL),
    /**
     * 字符串
     */
    VARCHAR(Types.VARCHAR),
    /**
     * 时间戳
     */
    TIMESTAMP(Types.TIMESTAMP);

    public final int TYPE_CODE;
    private static final Map<Integer,JdbcType> CODE_LOOKUP = new HashMap<>();

    // 就将数字对应的枚举型放入 HashMap
    static {
        for (JdbcType type : JdbcType.values()) {
            CODE_LOOKUP.put(type.TYPE_CODE, type);
        }
    }

    JdbcType(int code) {
        this.TYPE_CODE = code;
    }

    public static JdbcType forCode(int code)  {
        return CODE_LOOKUP.get(code);
    }

}

