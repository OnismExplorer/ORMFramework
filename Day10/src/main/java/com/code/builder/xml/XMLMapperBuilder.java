package com.code.builder.xml;

import com.code.builder.BaseBuilder;
import com.code.builder.MapperBuilderAssistant;
import com.code.io.Resources;
import com.code.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

/**
 * XML映射生成器
 *
 * @author HeXin
 * @date 2024/02/07
 */
public class XMLMapperBuilder extends BaseBuilder {

    /**
     * 元素
     */
    private Element element;

    /**
     * 资源
     */
    private String resource;

    /**
     * 映射器构建助手
     */
    private MapperBuilderAssistant builderAssistant;

    private XMLMapperBuilder(Document document, Configuration configuration, String resource) {
        super(configuration);
        this.element = document.getRootElement();
        this.resource = resource;
        this.builderAssistant = new MapperBuilderAssistant(configuration,resource);
    }

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource) throws DocumentException {
        this(new SAXReader().read(inputStream),configuration,resource);
    }

    /**
     * 解析
     *
     * @throws Exception 异常
     */
    public void parse() throws Exception {
        // 检查资源是否已加载过
        if (!configuration.isResourceLoaded(resource)) {
            configurationElement(element);

            // 将当前资源标记为已加载
            configuration.addLoadedResource(resource);

            // 绑定 Mapper 映射器到命名空间
            configuration.addMapper(Resources.classForName(builderAssistant.getCurrentNameSpace()));
        }
    }

    /**
     * 配置 Mapper 元素
     *
     * @param element 元素
     */
    public void configurationElement(Element element) {
        // 配置命名空间
        String namespace = element.attributeValue("namespace");
        if(namespace.equals("")) {
            throw new RuntimeException("Mapper 映射不能为空");
        }
        builderAssistant.setCurrentNameSpace(namespace);
        // 配置 select|insert|update|delete
        buildStatementFromContext(element.elements("select"));
    }

    /**
     * 根据上下文配置 select|insert|update|delete
     *
     * @param list 列表
     */
    public void buildStatementFromContext(List<Element> list) {
        for (Element e : list) {
            final XMLStatementBuilder builder = new XMLStatementBuilder(configuration,builderAssistant,e);
            builder.parseStatementNode();
        }
    }
}
