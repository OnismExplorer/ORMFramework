package com.code.script;

import java.util.HashMap;
import java.util.Map;

/**
 * 脚本语言注册器
 *
 * @author HeXin
 * @date 2024/02/07
 */
public class LanguageDriverRegistry {

    private final Map<Class<?>,LanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<>();

    private Class<?> defaultDriverClass = null;

    public void register(Class<?> clazz) {
        if(clazz == null) {
            throw new IllegalArgumentException("语言注册器为null！");
        }
        if(!LanguageDriver.class.isAssignableFrom(clazz)) {
            throw new RuntimeException(clazz.getName() + "没有实现" + LanguageDriver.class.getName() + "接口");
        }
        // 若为注册过，则进行注册
        LanguageDriver driver = LANGUAGE_DRIVER_MAP.get(clazz);
        if(driver == null) {
            try {
                // 单例模式(一个 Class 只能有一个对应的 LanguageDriver)
                driver = (LanguageDriver) clazz.getDeclaredConstructor().newInstance();
                LANGUAGE_DRIVER_MAP.put(clazz,driver);
            } catch (Exception e) {
                System.err.println("无法加载 " + clazz.getName() + " 的语言驱动器，抛出异常："+e);
            }
        }
    }

    public LanguageDriver getDriver(Class<?> clazz) {
        return LANGUAGE_DRIVER_MAP.get(clazz);
    }

    /**
     * 获取默认驱动
     *
     * @return {@link LanguageDriver}
     */
    public LanguageDriver getDefaultDriver() {
        return getDriver(getDefaultDriverClass());
    }

    /**
     * 获取默认驱动类
     *
     * @return {@link Class}<{@link ?}>
     */
    public Class<?> getDefaultDriverClass() {
        return defaultDriverClass;
    }

    /**
     * 设置默认驱动类 <br>
     * Configuration()有调用，默认的为XMLLanguageDriver
     *
     * @param defaultDriverClass 默认驱动类
     */
    public void setDefaultDriverClass(Class<?> defaultDriverClass) {
        register(defaultDriverClass);
        this.defaultDriverClass = defaultDriverClass;
    }
}
