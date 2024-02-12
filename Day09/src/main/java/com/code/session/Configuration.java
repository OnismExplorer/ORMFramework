package com.code.session;

import com.code.binding.MapperRegistry;
import com.code.datasource.druid.DruidDataSourceFactory;
import com.code.datasource.pool.PoolDataSourceFactory;
import com.code.datasource.unpool.UnpoolDataSourceFactory;
import com.code.executor.Executor;
import com.code.executor.SimpleExecutor;
import com.code.executor.parameter.ParameterHandler;
import com.code.executor.resultset.DefaultResultSetHandler;
import com.code.executor.resultset.ResultSetHandler;
import com.code.executor.statement.PreparedStatementHandler;
import com.code.executor.statement.StatementHandler;
import com.code.mapping.BoundSql;
import com.code.mapping.Environment;
import com.code.mapping.MappedStatement;
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
     * 创建结果集处理器
     *
     * @param executor        执行者
     * @param mappedStatement 映射语句
     * @param boundSql        绑定 SQL
     * @return {@link ResultSetHandler}
     */
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
        return new DefaultResultSetHandler(executor,mappedStatement,boundSql);
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
    public StatementHandler newStatementHandler(Executor executor,MappedStatement mappedStatement,Object parameter,ResultHandler resultHandler,BoundSql boundSql) {
        return new PreparedStatementHandler(executor,mappedStatement,parameter,resultHandler,boundSql);
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
}
