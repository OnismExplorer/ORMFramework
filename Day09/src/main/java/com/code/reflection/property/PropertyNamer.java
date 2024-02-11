package com.code.reflection.property;

import java.util.Locale;

/**
 * 属性命名器
 *
 * @author HeXin
 * @date 2024/02/02
 */
public class PropertyNamer {
    private PropertyNamer(){

    }

    /**
     * 方法转换为属性
     *
     * @param name 名字
     * @return {@link String}
     */
    public static String methodToProperty(String name) {
        if(name.startsWith("is")) {
            name = name.substring(2);
        } else if(name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new RuntimeException("解析属性名 '" + name + "' 时发生错误。属性名应以 'is'、'get' 或 'set' 开头。");

        }

        // 若只有一个字母，则转换为小写。若大于一个字母，第二个字母非大写，转换为小写
        if(name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0,1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }

        return name;
    }

    /**
     * 开头判断 get/set/is
     * @param name 名字
     * @return boolean
     */
    public static boolean isProperty(String name) {
        return name.startsWith("get") || name.startsWith("set") || name.startsWith("is");
    }

    /**
     * 是 getter
     *
     * @param name 名字
     * @return boolean
     */
    public static boolean isGetter(String name) {
        return name.startsWith("get") || name.startsWith("is");
    }

    public static boolean isSetter(String name) {
        return name.startsWith("set");
    }
}
