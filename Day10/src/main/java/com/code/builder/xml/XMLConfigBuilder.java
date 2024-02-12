package com.code.builder.xml;

import com.code.builder.BaseBuilder;
import com.code.datasource.DataSourceFactory;
import com.code.io.Resources;
import com.code.mapping.Environment;
import com.code.session.Configuration;
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
            // 环境
            environmentsElement(root.element("environments"));
            // 解析映射器
            mapperElement(root.element("mappers"));
        } catch (Exception e) {
            throw new RuntimeException("SQL 映射器解析错误，造成的原因是："+e);
        }
        return configuration;
    }

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


    private void mapperElement(Element mappers) throws Exception{
        List<Element> mapperList = mappers.elements("mapper");
        for (Element element : mapperList) {
            String resource = element.attributeValue("resource");
            InputStream inputStream = Resources.getResourceAsStream(resource);

            // 在循环中每个 mapper 都重新创建一个 XMLMapperBuilder 进行解析
            XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(inputStream, configuration, resource);
            mapperBuilder.parse();
        }
    }

}
