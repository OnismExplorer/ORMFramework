package com.code.builder.xml;

import com.code.builder.BaseBuilder;
import com.code.builder.MapperBuilderAssistant;
import com.code.builder.ResultMapResolver;
import com.code.cache.Cache;
import com.code.io.Resources;
import com.code.mapping.ResultFlag;
import com.code.mapping.ResultMap;
import com.code.mapping.ResultMapping;
import com.code.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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

        // 配置缓存
        cacheElement(element.element("cache"));

        // 解析 resultMap
        resultMapElements(element.elements("resultMap"));

        // 配置 select|insert|update|delete
        buildStatementFromContext(
                element.elements("select"),
                element.elements("insert"),
                element.elements("update"),
                element.elements("delete")
        );
    }

    /**
     * 缓存元素
     *
     * @param element 元素
     */
    private void cacheElement(Element element) {
        if (element == null) {
            return;
        }
        // 基础配置信息
        String type = element.attributeValue("type", "PERPETUAL");
        Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
        // 缓存队列 FIFO
        String eviction = element.attributeValue("eviction", "FIFO");
        Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);
        Long flushInterval = Long.valueOf(element.attributeValue("flushInterval"));
        Integer size = Integer.valueOf(element.attributeValue("size"));
        boolean readWrite = !Boolean.parseBoolean(element.attributeValue("readOnly", "false"));
        boolean blocking = !Boolean.parseBoolean(element.attributeValue("blocking", "false"));

        // 解析额外属性信息；<property name="cacheFile" value="/tmp/xxx-cache.tmp"/>
        List<Element> elements = element.elements();
        Properties props = new Properties();
        for (Element e : elements) {
            props.setProperty(e.attributeValue("name"), e.attributeValue("value"));
        }
        // 构建缓存
        builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
    }

    /**
     * 结果映射元素
     *
     * @param resultMaps 结果地图
     */
    private void resultMapElements(List<Element> resultMaps) {
        for (Element resultMap : resultMaps) {
            try {
                resultMapElement(resultMap, Collections.emptyList());
            } catch (Exception ignore) {

            }
        }
    }

    /**
     * 解析 resultMap 节点元素，构建并返回 ResultMap 对象
     *
     * @param resultMap                要解析的 <resultMap> 元素
     * @param additionalResultMappings 附加的 ResultMapping 列表
     * @return 解析后的 ResultMap 对象
     */
    private ResultMap resultMapElement(Element resultMap, List<ResultMapping> additionalResultMappings) {
        // 获取 <resultMap> 元素的 id 和 type 属性值
        String id = resultMap.attributeValue("id");
        String type = resultMap.attributeValue("type");

        // 解析 type 属性值对应的 Class 对象
        Class<?> typeClass = resolveClass(type);

        // 创建一个 ResultMapping 列表，并添加附加的 ResultMapping 列表
        List<ResultMapping> resultMappings = new ArrayList<>(additionalResultMappings);

        // 获取 <resultMap> 元素下的所有子元素
        List<Element> resultChildren = resultMap.elements();
        for (Element resultChild : resultChildren) {
            // 创建一个 ResultFlag 列表，用于标识 ResultMapping 的属性
            List<ResultFlag> flags = new ArrayList<>();

            // 如果当前子元素是 <id> 元素，则添加 ID 标志
            if ("id".equals(resultChild.getName())) {
                flags.add(ResultFlag.ID);
            }

            // 从当前子元素构建一个 ResultMapping，并添加到 resultMappings 列表中
            resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
        }

        // 创建 ResultMapResolver 对象，用于解析 ResultMap
        ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, resultMappings);

        // 调用 ResultMapResolver 的 resolve() 方法解析 ResultMap，并返回解析后的 ResultMap 对象
        return resultMapResolver.resolve();
    }

    private ResultMapping buildResultMappingFromContext(Element resultChild, Class<?> resultType, List<ResultFlag> flags) {
        String property = resultChild.attributeValue("property");
        String column = resultChild.attributeValue("column");
        return builderAssistant.buildResultMapping(resultType,property,column,flags);
    }


    /**
     * 根据上下文配置 select|insert|update|delete
     *
     * @param lists 列表
     */
    @SafeVarargs
    public final void buildStatementFromContext(List<Element>... lists) {
        for (List<Element> list : lists) {
            for (Element e : list) {
                final XMLStatementBuilder builder = new XMLStatementBuilder(configuration,builderAssistant,e);
                builder.parseStatementNode();
            }
        }
    }
}
