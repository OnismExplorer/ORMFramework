package com.code.builder;

import com.code.mapping.*;
import com.code.script.LanguageDriver;
import com.code.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 映射构建器助手(建造者)
 *
 * @author HeXin
 * @date 2024/02/12
 */
public class MapperBuilderAssistant extends BaseBuilder{

    /**
     * 当前名称空间
     */
    private String currentNameSpace;

    /**
     * 资源
     */
    private String resource;

    public MapperBuilderAssistant(Configuration configuration,String resource) {
        super(configuration);
        this.resource = resource;
    }

    public String getCurrentNameSpace() {
        return currentNameSpace;
    }

    public void setCurrentNameSpace(String currentNameSpace) {
        this.currentNameSpace = currentNameSpace;
    }

    /**
     * 应用当前名称空间
     *
     * @param base        基地
     * @param isReference 是否参考
     * @return {@link String}
     */
    public String applyCurrentNameSpace(String base,boolean isReference) {
        if(base == null) {
            return null;
        }
        if(isReference && base.contains(".")) {
            return base;
        } else {
            if(base.startsWith(currentNameSpace + ".")) {
                return base;
            }
            if(base.contains(".")) {
                throw new RuntimeException("元素名称中不允许出现点号，请从 " + base + " 中删除。");
            }
        }
        return currentNameSpace + "." + base;
    }

    /**
     * 添加映射语句
     *
     * @param id             id
     * @param sqlSource      sql源
     * @param sqlCommandType SQL命令类型
     * @param parameterType  参数类型
     * @param resultMap      结果图
     * @param resultType     结果类型
     * @param languageDriver 语言司机
     * @return {@link MappedStatement}
     */
    public MappedStatement addMappedStatement(String id, SqlSource sqlSource, SqlCommandType sqlCommandType, Class<?> parameterType, String resultMap, Class<?> resultType, LanguageDriver languageDriver) {
        // 为id加上namespace前缀(com.code.test.dao.UserDao.getUserById)
        id = applyCurrentNameSpace(id,false);
        MappedStatement.Builder builder = new MappedStatement.Builder(configuration, id, sqlCommandType, sqlSource, resultType);
        // 结果映射
        setStatementResultMap(resultMap,resultType,builder);

        MappedStatement statement = builder.build();
        // 映射语句信息，建造完成后存储在配置项中
        configuration.addMappedStatement(statement);
        return statement;
    }

    /**
     * 集合语句结果映射
     *暂时没有在
     * @param resultMap  结果图
     * @param resultType 结果类型
     * @param builder    构建器
     */
    private void setStatementResultMap(String resultMap, Class<?> resultType, MappedStatement.Builder builder) {
        resultMap = applyCurrentNameSpace(resultMap,true);
        List<ResultMap> resultMaps  = new ArrayList<>();

        if(resultMap != null) {
            String[] resultMapNames = resultMap.split(",");
            for (String resultMapName : resultMapNames) {
                resultMaps.add(configuration.getResultMap(resultMapName.trim()));
            }
        } else if(resultType != null) {
            ResultMap.Builder inllineResultMapBuilder = new ResultMap.Builder(configuration,builder.id() + "-Inline",resultType,new ArrayList<>());
            resultMaps.add(inllineResultMapBuilder.build());
        }
        builder.resultMaps(resultMaps);
    }

    /**
     * 添加结果映射
     *
     * @param id             id
     * @param type           类型
     * @param resultMappings 结果映射
     */
    public void addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings) {
        ResultMap.Builder builder = new ResultMap.Builder(configuration, id, type, resultMappings);
        ResultMap resultMap = builder.build();
        configuration.addResultMap(resultMap);
    }
}
