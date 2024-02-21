package com.code.session;

import com.code.binding.MapperRegistry;
import com.code.datasource.druid.DruidDataSourceFactory;
import com.code.datasource.pool.PoolDataSourceFactory;
import com.code.datasource.unpool.UnpoolDataSourceFactory;
import com.code.executor.Executor;
import com.code.executor.SimpleExecutor;
import com.code.executor.keygen.KeyGenerator;
import com.code.executor.parameter.ParameterHandler;
import com.code.executor.resultset.DefaultResultSetHandler;
import com.code.executor.resultset.ResultSetHandler;
import com.code.executor.statement.PreparedStatementHandler;
import com.code.executor.statement.StatementHandler;
import com.code.mapping.BoundSql;
import com.code.mapping.Environment;
import com.code.mapping.MappedStatement;
import com.code.mapping.ResultMap;
import com.code.plugin.Interceptor;
import com.code.plugin.InterceptorChain;
import com.code.reflection.MetaObject;
import com.code.reflection.factory.DefaultObjectFactory;
import com.code.reflection.factory.ObjectFactory;
import com.code.reflection.wrapper.DefaultObjectWrapperFactory;
import com.code.reflection.wrapper.ObjectWrapperFactory;
import com.code.script.LanguageDriver;
import com.code.script.LanguageDriverRegistry;
import com.code.script.xmltags.XMLLanguageDriver;
import com.code.transaction.Transaction;
import com.code.transaction.jdbc.JdbcTrasactionFactory;
import com.code.type.TypeAliasRegistry;
import com.code.type.TypeHandlerRegistry;

import java.util.*;

/**
 * 配置类
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class Configuration {

    /**
     * 环境类
     */
    protected Environment environment;

    /**
     * 映射器注册器
     */
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * 将映射语句存入在 Map 中
     */
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

    /**
     * 结果映射
     */
    protected final Map<String, ResultMap> resultMaps = new HashMap<>();

    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    /**
     * 语言驱动注册器
     */
    protected final LanguageDriverRegistry languageDriverRegistry = new LanguageDriverRegistry();

    /**
     * 类型处理器注册机
     */
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    /**
     * 对象工厂
     */
    protected ObjectFactory objectFactory = new DefaultObjectFactory();

    /**
     * 对象包装器工厂
     */
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    /**
     * 加载资源
     */
    protected final Set<String> loadedResources = new HashSet<>();

    /**
     * 数据库id
     */
    protected String databaseId;

    /**
     * 使用生成键
     */
    protected boolean useGeneratedKeys = false;

    protected final Map<String, KeyGenerator> keyGenerators = new HashMap<>();

    /**
     * 拦截器链
     */
    private final InterceptorChain interceptorChain = new InterceptorChain();
    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTrasactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOL", PoolDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOL", UnpoolDataSourceFactory.class);

        languageDriverRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    }

    /**
     * 扫描注入映射器
     *
     * @param packageName 软件包名称
     */
    public void addMappers(String packageName){
        mapperRegistry.addMappers(packageName);
    }

    /**
     * 添加映射器
     *
     * @param type 类型
     */
    public <T> void addMapper(Class<T> type){
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type,SqlSession sqlSession){
        return mapperRegistry.getMapper(type,sqlSession);
    }

    public boolean hasMapper(Class<?> type){
        return mapperRegistry.hasMapper(type);
    }

    public void addMappedStatement(MappedStatement statement){
        mappedStatements.put(statement.getId(),statement);
    }

    public MappedStatement getMappedStatement(String id){
        return mappedStatements.get(id);
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getDataBaseId() {
        return databaseId;
    }

    /**
     * 获取结果映射
     *
     * @param id id
     * @return {@link ResultMap}
     */
    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public void addResultMap(ResultMap resultMap) {
        resultMaps.put(resultMap.getId(),resultMap);
    }

    /**
     * 创建结果集处理器
     *
     * @param executor        执行者
     * @param mappedStatement 映射语句
     * @param boundSql        绑定 SQL
     * @return {@link ResultSetHandler}
     */
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement,RowBounds rowBounds,ResultHandler resultHandler, BoundSql boundSql) {
        return new DefaultResultSetHandler(executor,mappedStatement,resultHandler,rowBounds,boundSql);
    }

    /**
     * 创建执行器
     *
     * @param transaction 事务
     * @return {@link Executor}
     */
    public Executor newExecutor(Transaction transaction) {
        return new SimpleExecutor(this,transaction);
    }

    /**
     * 创建语句处理器
     *
     * @param executor        执行器
     * @param mappedStatement 映射语句
     * @param parameter       参数
     * @param resultHandler   结果处理程序
     * @param boundSql        绑定 SQL
     * @return {@link StatementHandler}
     */
    public StatementHandler newStatementHandler(Executor executor,MappedStatement mappedStatement,Object parameter,RowBounds rowBounds,ResultHandler resultHandler,BoundSql boundSql) {
        // 创建语句处理器，Mybatis 添加路由 STATEMENT、PREPARED、CALLABLE ，默认只根据预处理进行实例化
        StatementHandler statementHandler = new PreparedStatementHandler(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
        // 嵌入插件，代理对象
        statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
        return statementHandler;
    }

    /**
     * 新建元对象
     *
     * @param object 对象
     * @return {@link MetaObject}
     */
    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object,objectFactory,objectWrapperFactory);
    }

    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    /**
     * 资源是否已加载
     *
     * @param resource 资源
     * @return boolean
     */
    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }

    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    public LanguageDriverRegistry getLanguageRegistry(){
        return languageDriverRegistry;
    }


    /**
     * 新参数处理器
     *
     * @param mappedStatement 映射语句
     * @param parameterObject 参数对象
     * @param boundSql        绑定sql
     * @return {@link ParameterHandler}
     */
    public ParameterHandler newParameterHandler(MappedStatement mappedStatement,Object parameterObject,BoundSql boundSql) {
        // 创建参数处理器
        return mappedStatement.getLanguageDriver().createParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    /**
     * 获取默认脚本语言实例
     *
     * @return {@link LanguageDriver}
     */
    public LanguageDriver getDefaultScriptLanguageInstance() {
        return languageDriverRegistry.getDefaultDriver();
    }

    /**
     * 获取对象工厂
     *
     * @return {@link ObjectFactory}
     */
    public ObjectFactory getObjectFactory(){
        return objectFactory;
    }

    /**
     * 添加生成器
     *
     * @param id           id
     * @param keyGenerator 键生成器
     */
    public void addKeyGenerator(String id,KeyGenerator keyGenerator) {
        keyGenerators.put(id,keyGenerator);
    }

    /**
     * 获取生成器
     *
     * @param id id
     * @return {@link KeyGenerator}
     */
    public KeyGenerator getKeyGenerator(String id) {
        return keyGenerators.get(id);
    }

    /**
     * 是否有生成器
     *
     * @param id id
     * @return boolean
     */
    public boolean hasKeyGenerator(String id) {
        return keyGenerators.containsKey(id);
    }

    /**
     * 是否为生成键
     *
     * @return boolean
     */
    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    /**
     * 设置使用生成键
     *
     * @param useGeneratedKeys 使用生成键
     */
    public void setUseGeneratedKeys(boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
    }
}
