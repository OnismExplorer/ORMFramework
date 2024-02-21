package com.code.script.xmltags;

import com.code.builder.BaseBuilder;
import com.code.mapping.SqlSource;
import com.code.script.defaults.RawSqlSource;
import com.code.session.Configuration;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML 脚本构建器
 *
 * @author HeXin
 * @date 2024/02/07
 */
public class XMLScriptBuilder extends BaseBuilder {

    private Element element;

    private boolean isDynamic;

    private Class<?> parameterType;

    /**
     * 节点处理程序映射
     */
    private final Map<String,NodeHandler> nodeHandlerMap = new HashMap<>();

    /**
     * 节点处理程序
     *
     * @author HeXin
     * @date 2024/02/20
     */
    private interface NodeHandler {
        /**
         * 处理节点
         *
         * @param nodeToHandle   要处理节点
         * @param targetContents 目标内容
         */
        void handleNode(Element nodeToHandle,List<SqlNode> targetContents);
    }

    /**
     * trim 标签处理程序
     *
     * @author HeXin
     * @date 2024/02/20
     */
    private class TrimHandler implements NodeHandler {

        /**
         * 处理动态标签，解析包含的 SQL 片段，并创建 TrimSqlNode 添加到目标内容列表中。
         *
         * @param nodeToHandle 待处理的 XML 元素节点
         * @param targetContents 目标内容列表，用于存储解析后的 SQL 片段
         */
        @Override
        public void handleNode(Element nodeToHandle, List<SqlNode> targetContents) {
            // 解析动态标签内包含的 SQL 片段
            List<SqlNode> contents = parseDynamicTags(nodeToHandle);

            // 创建 MixedSqlNode 用于存储解析后的 SQL 片段
            MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);

            // 获取修饰符相关属性值
            String prefix = nodeToHandle.attributeValue("prefix");
            String prefixOverrides = nodeToHandle.attributeValue("prefixOverrides");
            String suffix = nodeToHandle.attributeValue("suffix");
            String suffixOverrides = nodeToHandle.attributeValue("suffixOverrides");

            // 创建 TrimSqlNode，并添加到目标内容列表中
            TrimSqlNode trim = new TrimSqlNode(configuration, mixedSqlNode, prefix, prefixOverrides, suffix, suffixOverrides);
            targetContents.add(trim);
        }

    }

    /**
     * if 处理程序
     *
     * @author HeXin
     * @date 2024/02/20
     */
    private class IfHandler implements NodeHandler {

        /**
         * 处理条件判断标签，解析包含的 SQL 片段，并创建 IfSqlNode 添加到目标内容列表中。
         *
         * @param nodeToHandle 待处理的 XML 元素节点
         * @param targetContents 目标内容列表，用于存储解析后的 SQL 片段
         */
        @Override
        public void handleNode(Element nodeToHandle, List<SqlNode> targetContents) {
            // 解析条件判断标签内包含的 SQL 片段
            List<SqlNode> contents = parseDynamicTags(nodeToHandle);

            // 创建 MixedSqlNode 用于存储解析后的 SQL 片段
            MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);

            // 获取条件判断相关属性值
            String test = nodeToHandle.attributeValue("test");

            // 创建 IfSqlNode，并添加到目标内容列表中
            IfSqlNode ifSqlNode = new IfSqlNode(test, mixedSqlNode);
            targetContents.add(ifSqlNode);
        }

    }

    protected XMLScriptBuilder(Configuration configuration, Element element,Class<?> parameterType) {
        super(configuration);
        this.element = element;
        this.parameterType = parameterType;
        initNodeHandlerMap();
    }

    private void initNodeHandlerMap() {
        // 总共有9种标签(trim/where/set/foreach/if/choose/when/otherwise/bind)，目前只实现其中的trim与if
        nodeHandlerMap.put("trim",new TrimHandler());
        nodeHandlerMap.put("if",new IfHandler());
    }

    /**
     * 解析脚本节点
     *
     * @return {@link SqlSource}
     */
    public SqlSource parseScriptNode() {
        List<SqlNode> contents = parseDynamicTags(element);
        MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
        SqlSource sqlSource = null;
        // 判断是否为动态 SQL
        if(isDynamic) {
            sqlSource = new DynamicSqlSource(configuration,rootSqlNode);
        } else {
            sqlSource = new RawSqlSource(configuration,rootSqlNode,parameterType);
        }
        return sqlSource;
    }

    /**
     * 解析动态标签，将 SQL 语句中的动态标签解析为 SqlNode 列表。
     *
     * @param element 包含动态标签的 XML 元素
     * @return SqlNode 列表
     * @throws RuntimeException 如果 SQL 语句包含未知的 XML 元素节点
     */
    List<SqlNode> parseDynamicTags(Element element) {
        List<SqlNode> contents = new ArrayList<>();
        List<Node> children = element.content();
        for (Node child : children) {
            if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                String data = child.getText();
                TextSqlNode textSqlNode = new TextSqlNode(data);
                if (textSqlNode.isDynamic()) {
                    contents.add(textSqlNode);
                    isDynamic = true;
                } else {
                    contents.add(new StaticTextSqlNode(data));
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = child.getName();
                NodeHandler handler = nodeHandlerMap.get(nodeName);
                if (handler == null) {
                    throw new RuntimeException("SQL 语句中未知 XML 元素节点  <" + nodeName + ">");
                }
                handler.handleNode(element.element(child.getName()), contents);
                isDynamic = true;
            }
        }
        return contents;
    }

}
