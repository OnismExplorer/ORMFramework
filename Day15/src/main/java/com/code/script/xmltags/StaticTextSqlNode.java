package com.code.script.xmltags;

/**
 * 静态文本SQL节点
 *
 * @author HeXin
 * @date 2024/02/07
 */
public class StaticTextSqlNode implements SqlNode{

    private String text;

    public StaticTextSqlNode(String text) {
        this.text = text;
    }

    @Override
    public boolean apply(DynamicContext context) {
        // 将文本加入到 context
        context.appendSql(text);
        return true;
    }
}
