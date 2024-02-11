package com.code.script.xmltags;

import java.util.List;

/**
 * 混合SQL节点
 *
 * @author HeXin
 * @date 2024/02/07
 */
public class MixedSqlNode implements SqlNode{

    /**
     * 组合模式拥有一个 SqlNode 的 List 集合
     */
    private final List<SqlNode> contents;

    public MixedSqlNode(List<SqlNode> contents) {
        this.contents = contents;
    }

    @Override
    public boolean apply(DynamicContext context) {
        // 依次调用集合中每个元素的 apply
        contents.forEach(node -> node.apply(context));
        return true;
    }
}
