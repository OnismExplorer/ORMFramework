package com.code.cache;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 缓存的键
 *
 * @author HeXin
 * @date 2024/02/21
 */
public class CacheKey implements Cloneable, Serializable {

    /**
     * 默认乘数(减小 Hash 冲突发生的概率，帮助散列函数产生更好的分布)
     */
    private static final int DEFAULT_MULTIPLYER = 31;

    /**
     * 默认hashcode
     */
    private static final int DEFAULT_HASHCODE = 17;

    private int multiplier;

    private int hashcode;

    private long checkNum;
    private int count;

    private List<Object> updateList;
    /**
     * 空缓存键
     */
    public static final CacheKey NULL_CACHE_KEY = new NullCacheKey();

    public CacheKey() {
        this.hashcode = DEFAULT_HASHCODE;
        this.multiplier = DEFAULT_MULTIPLYER;
        this.count = 0;
        this.updateList = new ArrayList<>();
    }

    public CacheKey(Object... values){
        this();
        updateAll(values);
    }

    /**
     * 更新所有
     *
     * @param values 值
     */
    private void updateAll(Object[] values) {
        for (Object value : values) {
            update(value);
        }
    }

    /**
     * 更新
     *
     * @param object 对象
     */
    public void update(Object object) {
        if(object != null && object.getClass().isArray()) {
            int n = Array.getLength(object);
            for(int i = 0;i < n;i++) {
                Object obj = Array.get(object,i);
                doUpdate(obj);
            }
        } else {
            doUpdate(object);
        }
    }

    /**
     * 执行更新
     *
     * @param object 对象
     */
    private void doUpdate(Object object) {
        // 计算 Hash 校验码
        int baseHashCode = object == null ? 1 : object.hashCode();

        count++;
        checkNum += baseHashCode;
        baseHashCode *= count;

        hashcode = multiplier * hashcode + baseHashCode;

        updateList.add(object);
    }

    /**
     * 获取更新计数
     *
     * @return int
     */
    public int getUpdateCount() {
        return updateList.size();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof CacheKey cacheKey)) {
            return false;
        }

        if (hashcode != cacheKey.hashcode) {
            return false;
        }
        if (checkNum != cacheKey.checkNum) {
            return false;
        }
        if (count != cacheKey.count) {
            return false;
        }

        for (int i = 0; i < updateList.size(); i++) {
            Object thisObject = updateList.get(i);
            Object thatObject = cacheKey.updateList.get(i);
            if (thisObject == null) {
                if (thatObject != null) {
                    return false;
                }
            } else {
                if (!thisObject.equals(thatObject)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder().append(hashcode).append(':').append(checkNum);
        for (Object obj : updateList) {
            returnValue.append(':').append(obj);
        }

        return returnValue.toString();
    }

    /**
     * 克隆
     *
     * @return {@link CacheKey}
     * @throws CloneNotSupportedException 不支持克隆异常
     */
    @Override
    public CacheKey clone() throws CloneNotSupportedException {
        CacheKey clonedCacheKey = (CacheKey) super.clone();
        clonedCacheKey.updateList = new ArrayList<>(updateList);
        return clonedCacheKey;
    }
}
