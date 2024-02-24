package com.code.mapping;

import com.code.cache.Cache;
import com.code.cache.Impl.PerpetualCache;
import com.code.cache.decorator.FIFOCache;
import com.code.reflection.MetaObject;
import com.code.reflection.SystemMetaObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 缓存建设者
 *
 * @author HeXin
 * @date 2024/02/23
 */
public class CacheBuilder {
    private final String id;

    /**
     * 实现
     */
    private Class<? extends Cache> implementation;

    /**
     * 修饰符
     */
    private final List<Class<? extends Cache>> decorators;

    /**
     * 大小
     */
    private Integer size;

    /**
     * 明确时间间隔
     */
    private Long clearInterval;

    /**
     * 读 or 写
     */
    private boolean readWrite;

    /**
     * 属性
     */
    private Properties properties;

    /**
     * 是否阻塞
     */
    private boolean blocking;

    public CacheBuilder(String id) {
        this.id = id;
        this.decorators = new ArrayList<>();
    }

    /**
     * 实现
     *
     * @param implementation 实现
     * @return {@link CacheBuilder}
     */
    public CacheBuilder implementation(Class<? extends Cache> implementation) {
        this.implementation = implementation;
        return this;
    }

    /**
     * 添加修饰符
     *
     * @param decorator 装饰
     * @return {@link CacheBuilder}
     */
    public CacheBuilder addDecorator(Class<? extends Cache> decorator) {
        if(decorator != null) {
            decorators.add(decorator);
        }
        return this;
    }

    public CacheBuilder size(Integer size) {
        this.size = size;
        return this;
    }

    /**
     * 明确时间间隔
     *
     * @param clearInterval 明确时间间隔
     * @return {@link CacheBuilder}
     */
    public CacheBuilder clearInterval(Long clearInterval) {
        this.clearInterval = clearInterval;
        return this;
    }

    /**
     * 阅读写
     *
     * @param readWrite 阅读写
     * @return {@link CacheBuilder}
     */
    public CacheBuilder readWrite(boolean readWrite) {
        this.readWrite = readWrite;
        return this;
    }

    /**
     * 阻塞
     *
     * @param blocking 阻塞
     * @return {@link CacheBuilder}
     */
    public CacheBuilder blocking(boolean blocking) {
        this.blocking = blocking;
        return this;
    }

    /**
     * 属性
     *
     * @param properties 属性
     * @return {@link CacheBuilder}
     */
    public CacheBuilder properties(Properties properties) {
        this.properties = properties;
        return this;
    }

    public Cache build() {
        setDefaultImplementations();
        Cache cache = newBaseCacheInstance(implementation,id);
        setCacheProperties(cache);
        if(PerpetualCache.class.equals(cache.getClass())) {
            for (Class<? extends Cache> decorator : decorators) {
                // 装饰者模式包装
                cache = newCacheDecoratorInstance(decorator,cache);
                // 额外属性设置
                setCacheProperties(cache);
            }
        }
        return cache;
    }

    /**
     * 新建缓存装饰器实例
     *
     * @param cacheClass 装饰
     * @param cache     缓存
     * @return {@link Cache}
     */
    private Cache newCacheDecoratorInstance(Class<? extends Cache> cacheClass, Cache cache) {
        Constructor<? extends Cache> constructor = getCacheDecoratorConstructor(cacheClass);
        try {
            return constructor.newInstance(cache);
        } catch (Exception e) {
            throw new RuntimeException("无法实例化缓存装饰器 (" + cacheClass + ")。造成原因: " + e, e);
        }
    }

    /**
     * 设置缓存的属性值，根据提供的属性映射。
     *
     * @param cache 缓存实例
     * @throws RuntimeException 如果缓存属性的类型不受支持
     */
    private void setCacheProperties(Cache cache) {
        if (properties != null) {
            // 获取缓存对象的元对象
            MetaObject metaCache = SystemMetaObject.forObject(cache);

            // 遍历配置的属性映射
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                // 获取属性名和属性值
                String name = (String) entry.getKey();
                String value = (String) entry.getValue();

                // 判断元对象是否包含属性的设置方法
                if (metaCache.hasSetter(name)) {
                    // 获取属性的类型
                    Class<?> type = metaCache.getSetterType(name);

                    // 根据类型进行相应的类型转换和设置
                    if (String.class == type) {
                        metaCache.setValue(name, value);
                    } else if (int.class == type || Integer.class == type) {
                        metaCache.setValue(name, Integer.valueOf(value));
                    } else if (long.class == type || Long.class == type) {
                        metaCache.setValue(name, Long.valueOf(value));
                    } else if (short.class == type || Short.class == type) {
                        metaCache.setValue(name, Short.valueOf(value));
                    } else if (byte.class == type || Byte.class == type) {
                        metaCache.setValue(name, Byte.valueOf(value));
                    } else if (float.class == type || Float.class == type) {
                        metaCache.setValue(name, Float.valueOf(value));
                    } else if (boolean.class == type || Boolean.class == type) {
                        metaCache.setValue(name, Boolean.valueOf(value));
                    } else if (double.class == type || Double.class == type) {
                        metaCache.setValue(name, Double.valueOf(value));
                    } else {
                        // 如果属性类型不受支持，抛出运行时异常
                        throw new RuntimeException("不支持的缓存属性类型: '" + name + "'，类型为 " + type);
                    }
                }
            }
        }
    }


    /**
     * 新建基本缓存实例
     *
     * @param cacheClass 缓存类
     * @param id         id
     * @return {@link Cache}
     */
    private Cache newBaseCacheInstance(Class<? extends Cache> cacheClass, String id) {
        Constructor<? extends Cache> cacheConstructor = getBaseCacheConstructor(cacheClass);
        try {
            return cacheConstructor.newInstance(id);
        } catch (Exception e) {
            throw new RuntimeException("无法实例化缓存实现类 (" + cacheClass + ")。造成原因: " + e.getMessage(), e);
        }
    }

    /**
     * 获取基本缓存构造函数
     *
     * @param cacheClass 缓存类
     * @return {@link Constructor}<{@link ?} {@link extends} {@link Cache}>
     */
    private Constructor<? extends Cache> getBaseCacheConstructor(Class<? extends Cache> cacheClass) {
        try {
            return cacheClass.getConstructor(String.class);
        } catch (Exception e) {
            throw new RuntimeException("无效的基础缓存实现 (" + cacheClass + ")。" +
                    "基础缓存实现必须具有一个以String id为参数的构造函数。造成原因: " + e, e);
        }
    }

    /**
     * 获取缓存装饰器构造函数
     *
     * @param cacheClass 缓存类
     * @return {@link Constructor}<{@link ?} {@link extends} {@link Cache}>
     */
    private Constructor<? extends Cache> getCacheDecoratorConstructor(Class<? extends Cache> cacheClass) {
        try {
            return cacheClass.getConstructor(Cache.class);
        } catch (Exception e) {
            throw new RuntimeException("无效的缓存装饰器 (" + cacheClass + ")。" +
                    "缓存装饰器必须具有接受 Cache 实例作为参数的构造函数。造成原因: " + e.getMessage(), e);
        }
    }

    /**
     * 设置默认实现
     */
    private void setDefaultImplementations() {
        if(implementation == null) {
            implementation = PerpetualCache.class;
            if(decorators.isEmpty()) {
                decorators.add(FIFOCache.class);
            }
        }
    }
}
