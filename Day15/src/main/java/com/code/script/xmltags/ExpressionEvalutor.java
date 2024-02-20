package com.code.script.xmltags;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 表达式求值器
 *
 * @author HeXin
 * @date 2024/02/20
 */
public class ExpressionEvalutor {

    /**
     * 表达式获取布尔值
     *
     * @param expression      表达式
     * @param parameterObject 参数对象
     * @return boolean
     */
    public boolean evaluateBoolean(String expression,Object parameterObject) {
        // 调用 ognl
        Object value = OgnlCache.getValue(expression, parameterObject);
        if(value instanceof Boolean v) {
            // 如果是布尔类型
            return v;
        }

        if(value instanceof Number) {
            // 如果类型为 Number，判断是否不为0
            return !new BigDecimal(String.valueOf(value)).equals(BigDecimal.ZERO);
        }

        // 否则判断值是否为null值
        return value != null;
    }


    /**
     * 解析表达式到Iterable，foreach 调用
     *
     * @param expression      表达式
     * @param parameterObject 参数对象
     * @return {@link Iterable}<{@link ?}>
     */
    @SuppressWarnings("rawtypes")
    public Iterable<?> evaluateIterable(String expression,Object parameterObject) {
        Object value = OgnlCache.getValue(expression, parameterObject);
        if(value == null) {
            throw new RuntimeException("表达式 '" + expression + "' 评估为 null 值。");
        }
        if(value instanceof Iterable<?> v) {
            return v;
        }
        if(value.getClass().isArray()) {
            // 如果为数组，则将其变成 List<Object>
            int size = Array.getLength(value);
            List<Object> answer = new ArrayList<>();
            for(int i =0; i < size;i++) {
                Object object = Array.get(value, i);
                answer.add(object);
            }
            return answer;
        }

        if(value instanceof Map map) {
            return map.entrySet();
        }
        throw new RuntimeException("评估表达式 '" + expression + "' 时发生错误。返回值（" + value + "）不可迭代。");
    }
}
