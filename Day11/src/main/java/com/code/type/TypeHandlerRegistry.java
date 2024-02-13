package com.code.type;

import cn.hutool.core.collection.CollUtil;

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
public final class TypeHandlerRegistry {
    /**
     * JDBC类型处理程序映射
     */
    private final Map<JdbcType,TypeHandler<?>> JDBC_TYPE_HANDLER_MAP = new EnumMap<>(JdbcType.class);

    private final Map<Type,Map<JdbcType,TypeHandler<?>>> TYPE_HANDLER_MAP = new HashMap<>();

    private final Map<Class<?>,TypeHandler<?>> ALL_TYPE_HANDLERS_MAP = new HashMap<>();

    public TypeHandlerRegistry() {
        register(Long.class, new LongTypeHandler());
        register(long.class, new LongTypeHandler());

        register(String.class, new StringTypeHandler());
        register(String.class, JdbcType.CHAR, new StringTypeHandler());
        register(String.class, JdbcType.VARCHAR, new StringTypeHandler());
    }

    /**
     * 注册类型处理器
     *
     * @param javaType    java类型
     * @param typeHandler 类型处理程序
     */
    private <T> void register(Type javaType,TypeHandler<? extends T> typeHandler) {
        register(javaType,null,typeHandler);
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

    /**
     * 获取类型处理器
     *
     * @param type     类型
     * @param jdbcType jdbc类型
     * @return {@link TypeHandler}<{@link T}>
     */
    @SuppressWarnings("unchecked")
    public <T> TypeHandler<T> getTypeHandler(Class<T> type,JdbcType jdbcType) {
        return getTypeHandler((Type) type,jdbcType);
    }

    @SuppressWarnings("unchecked")
    private  <T> TypeHandler<T> getTypeHandler(Type type,JdbcType jdbcType) {
        Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = TYPE_HANDLER_MAP.get(type);
        TypeHandler<?> handler = null;
        if(CollUtil.isNotEmpty(jdbcHandlerMap)) {
            handler = jdbcHandlerMap.get(jdbcType);
            if(handler == null) {
                handler = jdbcHandlerMap.get(null);
            }
        }
        return (TypeHandler<T>) handler;
    }

    /**
     * 是否有类型处理器
     *
     * @param javaType java类型
     * @return boolean
     */
    public boolean hasTypeHandler(Class<?> javaType) {
        return hasTypeHandler(javaType,null);
    }

    /**
     * 是否有类型处理器
     *
     * @param javaType java类型
     * @param jdbcType jdbc类型
     * @return boolean
     */
    public boolean hasTypeHandler(Class<?> javaType,JdbcType jdbcType) {
        return javaType != null && getTypeHandler((Type) javaType,jdbcType) != null;
    }
}
