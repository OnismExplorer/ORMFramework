package com.code.builder;

import com.code.mapping.MappedStatement;
import com.code.mapping.ResultMap;
import com.code.mapping.SqlCommandType;
import com.code.mapping.SqlSource;
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
        }
        return currentNameSpace + "." + base;
    }

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
            //TODO：现在暂时没有 Map 结果映射处理配置
        } else if(resultType != null) {
            ResultMap.Builder inllineResultMapBuilder = new ResultMap.Builder(configuration,builder.id() + "-Inline",resultType,new ArrayList<>());
            resultMaps.add(inllineResultMapBuilder.build());
        }
        builder.resultMaps(resultMaps);
    }
}
