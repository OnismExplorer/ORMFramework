package com.code.script.xmltags;

import com.code.builder.BaseBuilder;
import com.code.mapping.SqlSource;
import com.code.script.defaults.RawSqlSource;
import com.code.session.Configuration;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

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
    protected XMLScriptBuilder(Configuration configuration, Element element,Class<?> parameterType) {
        super(configuration);
        this.element = element;
        this.parameterType = parameterType;
    }

    /**
     * 解析脚本节点
     *
     * @return {@link SqlSource}
     */
    public SqlSource parseScriptNode() {
        List<SqlNode> contents = parseDynamicTags(element);
        MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
        return new RawSqlSource(configuration,rootSqlNode,parameterType);
    }

    List<SqlNode> parseDynamicTags(Element element) {
        List<SqlNode> contents = new ArrayList<>();
        // 通过 elemet.getText 获取到 SQL 语句
        String data = element.getText();
        contents.add(new StaticTextSqlNode(data));
        return contents;
    }
}
