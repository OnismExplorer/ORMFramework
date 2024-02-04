package com.code.reflection.property;

import java.util.Iterator;

/**
 * 属性分解标记
 *
 * @author HeXin
 * @date 2024/02/02
 */
public class PropertyTokenizer implements Iterable<PropertyTokenizer>, Iterator<PropertyTokenizer> {

    /**
     * 名字
     */
    private String name;

    /**
     * 索引名称
     */
    private String indexName;

    /**
     * 位置索引
     */
    private String index;

    /**
     * 子属性
     */
    private String children;

    /**
     * 属性解析器，用于解析属性的全名。
     *
     * @param fullname 完整的属性名，可能包含嵌套的子属性
     */
    public PropertyTokenizer(String fullname) {
        // 查找属性名中的"."的位置
        int delim = fullname.indexOf(".");

        // 如果属性名中包含"."，则分割成属性名和子属性
        if (delim > -1) {
            // 获取属性名
            name = fullname.substring(0, delim);

            // 获取子属性
            children = fullname.substring(delim + 1);
        } else {
            // 如果属性名中没有"."，则整个字符串为属性名，子属性为null
            name = fullname;
            children = null;
        }

        // 初始化索引名称为属性名
        indexName = name;

        // 查找属性名中的"["的位置
        delim = name.indexOf("[");

        // 如果属性名中包含"["，则分割成索引和属性名
        if (delim > -1) {
            // 获取索引
            index = name.substring(delim + 1, name.length() - 1);

            // 获取真正的属性名
            name = name.substring(0, delim);
        }
    }

    public String getName() {
        return name;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getIndex() {
        return index;
    }

    public String getChildren() {
        return children;
    }

    @Override
    public Iterator<PropertyTokenizer> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return children != null;
    }

    @Override
    public PropertyTokenizer next() {
        return new PropertyTokenizer(children);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("不支持移除操作，因为在属性的上下文中没有意义。");
    }
}
