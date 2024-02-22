package com.code.script.xmltags;

import ognl.Ognl;
import ognl.OgnlException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OGNL 缓存
 *
 * &emsp;&emsp;ognl缓存：http://code.google.com/p/mybatis/issues/detail?id=342 <br>
 * &emsp;&emsp;OGNL 是 Object-Graph Navigation Language 的缩写，它是一种功能强大的表达式语言（Expression Language，简称为EL）
 * 通过它简单一致的表达式语法，可以存取对象的任意属性，调用对象的方法，遍历整个对象的结构图，实现字段类型转化等功能。
 * 它使用相同的表达式去存取对象的属性。
 * @author HeXin
 * @date 2024/02/20
 */
public class OgnlCache {

    /**
     * 表达式缓存
     */
    private static final Map<String,Object> expressionCache = new ConcurrentHashMap<>();

    private OgnlCache() {

    }

    /**
     * 获得值
     *
     * @param expression 表达式
     * @param root       根
     * @return {@link Object}
     */
    @SuppressWarnings("unchecked")
    public static Object getValue(String expression,Object root) {
        try {
            Map<Object,OgnlClassResolver> context = Ognl.createDefaultContext(root,new OgnlClassResolver());
            return Ognl.getValue(parseExpression(expression),context,root);
        } catch (OgnlException e) {
            throw new RuntimeException("在评估表达式 '" + expression + "' 时发生错误：" + e, e);
        }
    }

    /**
     * 解析表达式
     *
     * @param expression 表达式
     * @return {@link Object}
     * @throws OgnlException ognl异常
     */
    private static Object parseExpression(String expression) throws OgnlException {
        Object node = expressionCache.get(expression);
        if(node == null) {
            // 因为 OgnlParser.toLevelExpression 较为费时，所以将结果放入 ConcurrentHashMap 中
            node = Ognl.parseExpression(expression);
            expressionCache.put(expression,node);
        }
        return node;
    }
}
