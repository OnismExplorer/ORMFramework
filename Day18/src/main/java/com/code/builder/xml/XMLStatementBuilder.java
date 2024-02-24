package com.code.builder.xml;

import com.code.builder.BaseBuilder;
import com.code.builder.MapperBuilderAssistant;
import com.code.executor.keygen.Jdbc3KeyGenerator;
import com.code.executor.keygen.KeyGenerator;
import com.code.executor.keygen.NoKeyGenerator;
import com.code.executor.keygen.SelectKeyGenerator;
import com.code.mapping.MappedStatement;
import com.code.mapping.SqlCommandType;
import com.code.mapping.SqlSource;
import com.code.script.LanguageDriver;
import com.code.session.Configuration;
import org.dom4j.Element;

import java.util.List;
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


        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        boolean flushCache = Boolean.parseBoolean(element.attributeValue("flushCache",String.valueOf(!isSelect)));
        boolean useCache = Boolean.parseBoolean(element.attributeValue("useCache",String.valueOf(!isSelect)));

        // 获取默认语言驱动器
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        LanguageDriver languageDriver = configuration.getLanguageRegistry().getDriver(langClass);

        // 解析 selectKey
        processSelectKeyNodes(id,parameterTypeClass,languageDriver);

        // 创建 SqlSource 对象
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, element, parameterTypeClass);

        // 仅对 insert 插入语句有效
        String keyProperty = element.attributeValue("keyProperty");
        KeyGenerator keyGenerator = null;
        String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        keyStatementId = builderAssistant.applyCurrentNameSpace(keyStatementId,true);

        if(configuration.hasKeyGenerator(keyStatementId)) {
            keyGenerator = configuration.getKeyGenerator(keyStatementId);
        } else {
            keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
        }
        // 调用助手类
        builderAssistant.addMappedStatement(id,sqlSource,sqlCommandType,parameterTypeClass,resultMap,resultTypeClass,flushCache,useCache,keyGenerator,keyProperty,languageDriver);
    }

    /**
     * 选择关键节点流程
     *
     * @param id                 id
     * @param parameterTypeClass 参数类型
     * @param languageDriver     语言司机
     */
    private void processSelectKeyNodes(String id, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        List<Element> selectKeyNodes = element.elements("selectKey");
        parseStatementNodes(id,selectKeyNodes,parameterTypeClass,languageDriver);
    }

    /**
     * 解析语句节点
     *
     * @param parentId           父id
     * @param list               列表
     * @param parameterTypeClass 参数类型
     * @param languageDriver     脚本语言驱动
     */
    private void parseStatementNodes(String parentId,List<Element> list, Class<?> parameterTypeClass,LanguageDriver languageDriver) {
        for (Element nodeToHandle : list) {
            String id = parentId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
            parseStatementNode(id,nodeToHandle,parameterTypeClass,languageDriver);
        }
    }

    /**
     * 解析语句节点
     *
     * @param id                 id
     * @param nodeToHandle       要处理节点
     * @param parameterTypeClass 参数类型
     * @param languageDriver     脚本语言驱动
     */
    private void parseStatementNode(String id, Element nodeToHandle, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        String resultType = nodeToHandle.attributeValue("resultType");
        Class<?> resultTypeClass = resolveClass(resultType);
        boolean executeBefore = "BEFORE".equals(nodeToHandle.attributeValue("order","AFTER"));
        String keyProperty = nodeToHandle.attributeValue("keyProperty");

        // 默认值
        String resultMap = null;
        boolean flushCache = false;
        boolean useCache = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();

        // 解析为 SqlSource，DynamicSqlSource/RawSqlSource
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, nodeToHandle, parameterTypeClass);
        SqlCommandType select = SqlCommandType.SELECT;

        // 调用助手类
        builderAssistant.addMappedStatement(id,sqlSource,select,parameterTypeClass,resultMap,resultTypeClass,flushCache,useCache,keyGenerator,keyProperty,languageDriver);

        // 给 id 添加 namespace 前缀
        id = builderAssistant.applyCurrentNameSpace(id,false);

        // 存放键值生成器配置
        MappedStatement keyStatement = configuration.getMappedStatement(id);
        configuration.addKeyGenerator(id,new SelectKeyGenerator(executeBefore,keyStatement));
    }
}
