package com.code.script.xmltags;

/**
 * sql节点
 *
 * @author HeXin
 * @date 2024/02/07
 */
public interface SqlNode {

    /**
     * 应用
     *
     * @param context 上下文
     * @return boolean
     */
    boolean apply(DynamicContext context);
}
