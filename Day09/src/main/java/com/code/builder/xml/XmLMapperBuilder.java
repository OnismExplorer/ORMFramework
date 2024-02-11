package com.code.builder.xml;

import com.code.builder.BaseBuilder;
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
public class XmLMapperBuilder extends BaseBuilder {

    /**
     * 元素
     */
    private Element element;

    /**
     * 资源
     */
    private String resource;

    /**
     * 当前名称空间
     */
    private String currentNamespace;

    private XmLMapperBuilder(Document document,Configuration configuration,String resource) {
        super(configuration);
        this.element = document.getRootElement();
        this.resource = resource;
    }

    public XmLMapperBuilder(InputStream inputStream,Configuration configuration,String resource) throws DocumentException {
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
            configuration.addMapper(Resources.classForName(currentNamespace));
        }
    }

    /**
     * 配置 Mapper 元素
     *
     * @param element 元素
     */
    public void configurationElement(Element element) {
        // 配置命名空间
        currentNamespace = element.attributeValue("namespace");
        if(currentNamespace.equals("")) {
            System.err.println("Mapper 映射不能为空");
        }

        // 配置 select|insert|update|delete
        buildStatementFromContext(element.elements("select"));
    }

    /**
     * 根据上下文配置 select|insert|update|delete
     *
     * @param list 列表
     */
    public void buildStatementFromContext(List<Element> list) {
        for (Element element : list) {
            final XMLStatementBuilder builder = new XMLStatementBuilder(configuration,element,currentNamespace);
            builder.parseStatementNode();
        }
    }
}
