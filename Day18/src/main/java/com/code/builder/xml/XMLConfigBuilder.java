package com.code.builder.xml;

import com.code.builder.BaseBuilder;
import com.code.datasource.DataSourceFactory;
import com.code.io.Resources;
import com.code.mapping.Environment;
import com.code.plugin.Interceptor;
import com.code.session.Configuration;
import com.code.session.LocalCacheScope;
import com.code.transaction.TransactionFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * XML 配置构建器
 *
 * @author HeXin
 * @date 2024/02/07
 */
public class XMLConfigBuilder extends BaseBuilder {
    private Element root;

    public XMLConfigBuilder(Reader reader){
        // 调用父类初始化 Configuration
        super(new Configuration());
        // 用 DOM4j处理 xml
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(new InputSource(reader));
            root = document.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析配置
     *
     * @return {@link Configuration}
     */
    public Configuration parse(){
        // 解析映射器
        try {
            // 设置(缓存)
            settingElement(root.element("settings"));
            // 插件
            pluginElement(root.element("plugins"));
            // 环境
            environmentsElement(root.element("environments"));
            // 解析映射器
            mapperElement(root.element("mappers"));
        } catch (Exception e) {
            throw new RuntimeException("SQL 映射器解析错误，造成的原因是："+e);
        }
        return configuration;
    }

    /**
     * 解析 XML 环境配置元素
     *
     * @param context 上下文
     * @throws Exception 异常
     */
    private void environmentsElement(Element context) throws Exception {
        String environment = context.attributeValue("default");

        List<Element> environmentList = context.elements("environment");
        for (Element e : environmentList) {
            String id = e.attributeValue("id");
            if (environment.equals(id)) {
                // 事务管理器
                TransactionFactory txFactory = (TransactionFactory) typeAliasRegistry.resolveAlias(e.element("transactionManager").attributeValue("type")).getDeclaredConstructor().newInstance();

                // 数据源
                Element dataSourceElement = e.element("dataSource");
                DataSourceFactory dataSourceFactory = (DataSourceFactory) typeAliasRegistry.resolveAlias(dataSourceElement.attributeValue("type")).getDeclaredConstructor().newInstance();
                List<Element> propertyList = dataSourceElement.elements("property");
                Properties props = new Properties();
                for (Element property : propertyList) {
                    props.setProperty(property.attributeValue("name"), property.attributeValue("value"));
                }
                dataSourceFactory.setProperties(props);
                DataSource dataSource = dataSourceFactory.getDataSource();

                // 构建环境
                Environment.Builder environmentBuilder = new Environment.Builder(id)
                        .transactionFactory(txFactory)
                        .dataSource(dataSource);

                configuration.setEnvironment(environmentBuilder.build());
            }
        }
    }


    /**
     * 解析 Mapper XML 元素
     *
     * @param mappers 映射器
     * @throws Exception 异常
     */
    private void mapperElement(Element mappers) throws Exception{
        List<Element> mapperList = mappers.elements("mapper");
        for (Element element : mapperList) {
            String resource = element.attributeValue("resource");

            // 解析注解XML配置
            String mapperClass = element.attributeValue("class");

            if(resource != null && mapperClass == null) {
                InputStream inputStream = Resources.getResourceAsStream(resource);
                // 在循环中每个 mapper 都重新创建一个 XMLMapperBuilder 进行解析
                XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(inputStream, configuration, resource);
                mapperBuilder.parse();
            } else if (resource == null && mapperClass != null) {
                Class<?> mapperInterface = Resources.classForName(mapperClass);
                // 将其加入到映射中
                configuration.addMapper(mapperInterface);
            }

        }
    }

    /**
     * 允许在某一点切入映射语句执行的调度
     *
     * @param parent 家长。
     * @throws Exception 异常
     */
    private void pluginElement(Element parent) throws Exception {
        if(parent == null) {
            return ;
        }
        List<Element> elements = parent.elements();
        for (Element element : elements) {
            String interceptor = element.attributeValue("interceptor");
            // 参数配置
            Properties properties = new Properties();
            List<Element> propertyElementList = element.elements("property");
            for (Element property : propertyElementList) {
                properties.setProperty(property.attributeValue("name"), property.attributeValue("value"));
            }
            // 获取插件实现类并将其实例化
            Interceptor newInstance = (Interceptor) resolveClass(interceptor).getDeclaredConstructor().newInstance();
            newInstance.setProperties(properties);
            configuration.addInterceptor(newInstance);
        }
    }

    /**
     * setting 元件
     *
     * @param element 元素
     */
    private void settingElement(Element element) {
        if(element == null) {
            return ;
        }
        List<Element> elements = element.elements();
        Properties properties = new Properties();
        for (Element e : elements) {
            properties.setProperty(e.attributeValue("name"),e.attributeValue("value"));
        }
        configuration.setCacheEnabled(booleanValueOf(properties.getProperty("cacheEnabled"),true));
        configuration.setLocalCacheScope(LocalCacheScope.valueOf(properties.getProperty("localCacheScope")));
    }
}
