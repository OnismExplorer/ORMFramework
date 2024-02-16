package com.code.reflection.wrapper;

import com.code.reflection.MetaObject;
import com.code.reflection.property.PropertyTokenizer;

import java.util.List;
import java.util.Map;

/**
 * 对象包装器抽象基类(负责提供一些工具方法)
 *
 * @author HeXin
 * @date 2024/02/02
 */
public abstract class BaseWrapper implements ObjectWrapper{
    /**
     * 没有参数
     */
    protected static final Object[] NO_ARGUMENTS = new Object[0];

    /**
     * 元对象
     */
    protected MetaObject metaObject;

    public BaseWrapper(MetaObject metaObject) {
        this.metaObject = metaObject;
    }

    /**
     * 解析集合
     * @param tokenizer 熟悉分解标记
     * @param object    对象
     * @return {@link Object}
     */
    protected Object resolveCollection(PropertyTokenizer tokenizer,Object object) {
        if("".equals(tokenizer.getName())) {
            return object;
        }
        return metaObject.getValue(tokenizer.getName());
    }

    /**
     * 获取集合的值
     * @param tokenizer  属性分解标记
     * @param collection 集合
     * @return {@link Object}
     */
    @SuppressWarnings("all")
    protected Object getCollectionValue(PropertyTokenizer tokenizer,Object collection) {
        Object result;
        if (collection instanceof Map) {
            // map['name']
            result = ((Map) collection).get(tokenizer.getIndex());
        } else {
            int i = Integer.parseInt(tokenizer.getIndex());
            result = switch (getTypeCode(collection)) {
                case 'L' ->
                    // list[0]
                        ((List) collection).get(i);
                case 'O' ->
                    // array[index]
                        ((Object[]) collection)[i];
                case 'C' ->
                    // char array[index]
                        ((char[]) collection)[i];
                case 'Z' ->
                    // boolean array[index]
                        ((boolean[]) collection)[i];
                case 'B' ->
                    // byte array[index]
                        ((byte[]) collection)[i];
                case 'D' ->
                    // double array[index]
                        ((double[]) collection)[i];
                case 'F' ->
                    // float array[index]
                        ((float[]) collection)[i];
                case 'I' ->
                    // int array[index]
                        ((int[]) collection)[i];
                case 'J' ->
                    // long array[index]
                        ((long[]) collection)[i];
                case 'S' ->
                    // short array[index]
                        ((short[]) collection)[i];
                default ->
                        throw new RuntimeException(collection + "的 '" + tokenizer.getName() + "' 属性不是 List 或 Array 类型。");
            };
        }
        return result;
    }

    /**
     * 获取集合类型代号
     * @param collection 集合
     * @return char
     */
    @SuppressWarnings("all")
    private char getTypeCode(Object collection) {
        if (collection instanceof List) {
            return 'L';  // List
        } else if (collection instanceof Object[]) {
            return 'O';  // Object array
        } else if (collection instanceof char[]) {
            return 'C';  // Char array
        } else if (collection instanceof boolean[]) {
            return 'Z';  // Boolean array
        } else if (collection instanceof byte[]) {
            return 'B';  // Byte array
        } else if (collection instanceof double[]) {
            return 'D';  // Double array
        } else if (collection instanceof float[]) {
            return 'F';  // Float array
        } else if (collection instanceof int[]) {
            return 'I';  // Int array
        } else if (collection instanceof long[]) {
            return 'J';  // Long array
        } else if (collection instanceof short[]) {
            return 'S';  // Short array
        } else {
            throw new RuntimeException("不支持的集合类型：" + collection.getClass());
        }
    }

    @SuppressWarnings("all")
    protected void setCollectionValue(PropertyTokenizer tokenizer,Object collection,Object value) {
        switch (getTypeCode(collection)) {
            case 'M':
                // Map类型使用put方法设置属性值
                ((Map) collection).put(tokenizer.getIndex(), value);
                break;
            case 'L':
                // List类型使用set方法设置属性值
                ((List) collection).set(Integer.parseInt(tokenizer.getIndex()), value);
                break;
            case 'O':
                // Object[]类型使用数组索引设置属性值
                ((Object[]) collection)[Integer.parseInt(tokenizer.getIndex())] = value;
                break;
            case 'C':
                // char[]类型需要将value强制转换为Character，并通过数组索引设置属性值
                ((char[]) collection)[Integer.parseInt(tokenizer.getIndex())] = (Character) value;
                break;
            case 'Z':
                // boolean[]类型需要将value强制转换为Boolean，并通过数组索引设置属性值
                ((boolean[]) collection)[Integer.parseInt(tokenizer.getIndex())] = (Boolean) value;
                break;
            case 'B':
                // byte[]类型需要将value强制转换为Byte，并通过数组索引设置属性值
                ((byte[]) collection)[Integer.parseInt(tokenizer.getIndex())] = (Byte) value;
                break;
            case 'D':
                // double[]类型需要将value强制转换为Double，并通过数组索引设置属性值
                ((double[]) collection)[Integer.parseInt(tokenizer.getIndex())] = (Double) value;
                break;
            case 'F':
                // float[]类型需要将value强制转换为Float，并通过数组索引设置属性值
                ((float[]) collection)[Integer.parseInt(tokenizer.getIndex())] = (Float) value;
                break;
            case 'I':
                // int[]类型需要将value强制转换为Integer，并通过数组索引设置属性值
                ((int[]) collection)[Integer.parseInt(tokenizer.getIndex())] = (Integer) value;
                break;
            case 'J':
                // long[]类型需要将value强制转换为Long，并通过数组索引设置属性值
                ((long[]) collection)[Integer.parseInt(tokenizer.getIndex())] = (Long) value;
                break;
            case 'S':
                // short[]类型需要将value强制转换为Short，并通过数组索引设置属性值
                ((short[]) collection)[Integer.parseInt(tokenizer.getIndex())] = (Short) value;
                break;
            default:
                // 如果集合类型不支持设置属性值，则抛出异常
                throw new RuntimeException(collection + "的 '" + tokenizer.getName() + "' 属性不是 List 或 Array 类型。");
        }
    }
}
