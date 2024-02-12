package com.code.builder.xml;

import com.code.builder.BaseBuilder;
import com.code.builder.MapperBuilderAssistant;
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

    /**
     * 映射构建器助手
     */
    private MapperBuilderAssistant builderAssistant;
    private Element element;
    public XMLStatementBuilder(Configuration configuration,MapperBuilderAssistant builderAssistant,Element element) {
        super(configuration);
        this.element = element;
        this.builderAssistant = builderAssistant;
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

        // 外部应用 ResultMap
        String resultMap = element.attributeValue("resultMap");
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

        // 调用助手类
        builderAssistant.addMappedStatement(id,sqlSource,sqlCommandType,parameterTypeClass,resultMap,resultTypeClass,languageDriver);
    }

}
