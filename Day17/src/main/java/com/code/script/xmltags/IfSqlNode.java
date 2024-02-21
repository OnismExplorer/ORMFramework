package com.code.script.xmltags;

/**
 * if SQL节点
 *
 * @author HeXin
 * @date 2024/02/20
 */
public class IfSqlNode implements SqlNode{

    private ExpressionEvalutor evalutor;
    private String test;
    private SqlNode contents;

    public IfSqlNode(String test, SqlNode contents) {
        this.test = test;
        this.contents = contents;
        this.evalutor = new ExpressionEvalutor();
    }

    @Override
    public boolean apply(DynamicContext context) {
        // 如果满足条件则直接应用，并返回true
        if(evalutor.evaluateBoolean(test,context.getBindings())) {
            contents.apply(context);
            return true;
        }
        return false;
    }
}
