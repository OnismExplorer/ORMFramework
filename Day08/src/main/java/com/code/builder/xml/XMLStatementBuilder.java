package com.code.builder.xml;

import com.code.builder.BaseBuilder;
import com.code.mapping.MappedStatement;
import com.code.mapping.SqlCommandType;
import com.code.mapping.SqlSource;
import com.code.script.LanguageDriver;
import com.code.session.Configuration;
import org.dom4j.Element;

import java.util.Locale;

/**
 * XML 语句构建者
 *
 * @author HeXin
 * @date 2024/02/07
 */
public class XMLStatementBuilder extends BaseBuilder {

    private String currentNamespace;
    private Element element;
    public XMLStatementBuilder(Configuration configuration,Element element, String currentNamespace) {
        super(configuration);
        this.element = element;
        this.currentNamespace = currentNamespace;
    }

    /**
     * 解析 `select`、`insert`、`update`、`delete` 等语句节点，并构建 MappedStatement 对象。
     */
    public void parseStatementNode() {
        // 获取语句的 id 属性
        String id = element.attributeValue("id");

        // 获取参数类型
        String parameterType = element.attributeValue("parameterType");
        Class<?> parameterTypeClass = resolveAlias(parameterType);

        // 获取结果类型
        String resultType = element.attributeValue("resultType");
        Class<?> resultTypeClass = resolveAlias(resultType);

        // 获取命令类型(select|insert|update|delete)
        String nodeName = element.getName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

        // 获取默认语言驱动器
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        LanguageDriver languageDriver = configuration.getLanguageRegistry().getDriver(langClass);

        // 创建 SqlSource 对象
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, element, parameterTypeClass);

        // 构建 MappedStatement 对象
        MappedStatement mappedStatement = new MappedStatement.Builder(configuration, currentNamespace + "." + id, sqlCommandType, sqlSource, resultTypeClass).build();

        // 添加解析后的 MappedStatement 到 MyBatis 配置对象中
        configuration.addMappedStatement(mappedStatement);
    }

}
