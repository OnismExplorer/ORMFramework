package com.code.builder;

import com.code.cache.Cache;
import com.code.cache.Impl.PerpetualCache;
import com.code.cache.decorator.FIFOCache;
import com.code.executor.keygen.KeyGenerator;
import com.code.mapping.*;
import com.code.reflection.MetaClass;
import com.code.script.LanguageDriver;
import com.code.session.Configuration;
import com.code.type.TypeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

    /**
     * 当前缓存
     */
    private Cache currentCache;

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
    public MappedStatement addMappedStatement(String id, SqlSource sqlSource, SqlCommandType sqlCommandType,Class<?> parameterType, String resultMap, Class<?> resultType,boolean flushCache,boolean useCache, KeyGenerator keyGenerator,String keyProperty, LanguageDriver languageDriver) {
        // 为id加上namespace前缀(com.code.test.dao.UserDao.getUserById)
        id = applyCurrentNameSpace(id,false);

        // 是否为 select 语句
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

        MappedStatement.Builder builder = new MappedStatement.Builder(configuration, id, sqlCommandType, sqlSource, resultType);
        builder.resource(resource);
        builder.keyGenerator(keyGenerator);
        builder.keyProperty(keyProperty);

        // 结果映射
        setStatementResultMap(resultMap,resultType,builder);
        setStatementCache(isSelect,flushCache,useCache,currentCache,builder);

        MappedStatement statement = builder.build();
        // 映射语句信息，建造完成后存储在配置项中
        configuration.addMappedStatement(statement);
        return statement;
    }

    /**
     * 设置语句缓存
     *
     * @param isSelect     是选择
     * @param flushCache
     * @param useCache     使用缓存
     * @param currentCache 当前缓存
     * @param builder      构建器
     */
    private void setStatementCache(boolean isSelect, boolean flushCache, boolean useCache, Cache currentCache, MappedStatement.Builder builder) {
        flushCache = valueOrDefault(flushCache,!isSelect);
        useCache = valueOrDefault(useCache,isSelect);
        builder.flushCacheRequired(flushCache);
        builder.useCache(useCache);
        builder.cache(currentCache);
    }


    /**
     * 使用新缓存
     *
     * @param typeClass     类型类
     * @param evictionClass 拆迁类
     * @param flushInterval 冲洗时间间隔
     * @param size          大小
     * @param readWrite     阅读写
     * @param blocking      阻塞
     * @param properties    属性
     * @return {@link Cache}
     */
    public Cache useNewCache(Class<? extends Cache> typeClass, Class<? extends Cache> evictionClass, Long flushInterval, Integer size, boolean readWrite, boolean blocking, Properties properties) {
        // 判断为null，则用默认值
        typeClass = valueOrDefault(typeClass, PerpetualCache.class);
        evictionClass = valueOrDefault(evictionClass, FIFOCache.class);

        // 建造者模式构建 Cache
        Cache cache = new CacheBuilder(currentNameSpace)
                .implementation(typeClass)
                .addDecorator(evictionClass)
                .clearInterval(flushInterval)
                .size(size)
                .readWrite(readWrite)
                .blocking(blocking)
                .properties(properties)
                .build();

        // 添加缓存
        configuration.addCache(cache);
        currentCache = cache;
        return cache;
    }

    /**
     * 值或默认值
     *
     * @param value        价值
     * @param defaultValue 默认值
     * @return {@link T}
     */
    private <T> T valueOrDefault(T value,T defaultValue) {
        return value == null ? defaultValue : value;
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
     * @return
     */
    public ResultMap addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings) {
        // 补全 id 全路径
        id = applyCurrentNameSpace(id,false);

        ResultMap.Builder builder = new ResultMap.Builder(configuration, id, type, resultMappings);
        ResultMap resultMap = builder.build();
        configuration.addResultMap(resultMap);
        return resultMap;
    }

    /**
     * 构建结果映射
     *
     * @param resultType 结果类型
     * @param property   财产
     * @param column     列
     * @param flags      旗帜
     * @return {@link ResultMapping}
     */
    public ResultMapping buildResultMapping(Class<?> resultType,String property,String column,List<ResultFlag> flags) {
        Class<?> javaTypeClass = resolveResultJavaType(resultType,property,null);
        TypeHandler<?> typeHandler = resolveTypeHandler(javaTypeClass,null);

        ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property, column, javaTypeClass);
        builder.typeHandler(typeHandler);
        builder.flags(flags);

        return builder.build();
    }

    /**
     * 解析结果Java类型
     *
     * @param resultType 结果类型
     * @param property   财产
     * @param javaType   java类型
     * @return {@link Class}<{@link ?}>
     */
    private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType) {
        if(javaType == null && property != null) {
            try {
                MetaClass metaClass = MetaClass.forClass(resultType);
                javaType = metaClass.getSetterType(property);
            } catch (Exception ignore) {

            }
        }
        if(javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

}
