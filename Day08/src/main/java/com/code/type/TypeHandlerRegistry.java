package com.code.type;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * 类型处理器注册机
 *
 * @author HeXin
 * @date 2024/02/07
 */
public class TypeHandlerRegistry {
    /**
     * JDBC类型处理程序映射
     */
    private final Map<JdbcType,TypeHandler<?>> JDBC_TYPE_HANDLER_MAP = new EnumMap<>(JdbcType.class);

    private final Map<Type,Map<JdbcType,TypeHandler<?>>> TYPE_HANDLER_MAP = new HashMap<>();

    private final Map<Class<?>,TypeHandler<?>> ALL_TYPE_HANDLERS_MAP = new HashMap<>();

    public TypeHandlerRegistry() {
    }

    /**
     * 注册类型处理器，将 Java 类型、JDBC 类型以及对应的类型处理器关联起来。
     *
     * @param javaType Java 类型
     * @param jdbcType JDBC 类型
     * @param handler  类型处理器
     */
    private void register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) {
        // 如果 JDBC 类型不为 null
        if (javaType != null) {
            // 获取指定 Java 类型对应的 Map，如果不存在，则创建一个新的 Map
            Map<JdbcType, TypeHandler<?>> map = TYPE_HANDLER_MAP.computeIfAbsent(javaType, m -> new HashMap<>());

            // 将 JDBC 类型和类型处理器关联起来
            map.put(jdbcType, handler);
        }

        // 将类型处理器和其对应的类型处理器类关联起来
        ALL_TYPE_HANDLERS_MAP.put(handler.getClass(), handler);
    }

}
