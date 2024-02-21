package com.code.type;

import com.code.io.Resources;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 类型别名注册器
 * @author HeXin
 * @date 2024/01/26
 */
public class TypeAliasRegistry {
    private final Map<String, Class<?>> TYPE_ALIASES = new HashMap<>();

    public TypeAliasRegistry() {
        // 构造函数里注册系统内置的类型别名
        registerAlias("string", String.class);

        // 基本包装类型
        registerAlias("byte", Byte.class);
        registerAlias("long", Long.class);
        registerAlias("short", Short.class);
        registerAlias("int", Integer.class);
        registerAlias("integer", Integer.class);
        registerAlias("double", Double.class);
        registerAlias("float", Float.class);
        registerAlias("boolean", Boolean.class);
    }

    public void registerAlias(String alias, Class<?> value) {
        String key = alias.toLowerCase(Locale.ENGLISH);
        TYPE_ALIASES.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> resolveAlias(String string) {
        if (string == null) {
            return null;
        }
        String key = string.toLowerCase(Locale.ENGLISH);
        Class<T> value;
        if (TYPE_ALIASES.containsKey(key)) {
            value = (Class<T>) TYPE_ALIASES.get(key);
        } else {
            try {
                value = (Class<T>) Resources.classForName(string);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("未找到此类 "+string+"："+e);
            }
        }
        return value;

    }

}
