package com.code.executor.resultset;

import com.code.executor.Executor;
import com.code.executor.result.DefaultResultContext;
import com.code.executor.result.DefaultResultHandler;
import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.mapping.ResultMap;
import com.code.mapping.ResultMapping;
import com.code.reflection.MetaClass;
import com.code.reflection.MetaObject;
import com.code.reflection.factory.ObjectFactory;
import com.code.session.Configuration;
import com.code.session.ResultHandler;
import com.code.session.RowBounds;
import com.code.type.TypeHandler;
import com.code.type.TypeHandlerRegistry;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 默认 Map 结果集处理器
 *
 * @author HeXin
 * @date 2024/01/30
 */
public class DefaultResultSetHandler implements ResultSetHandler{

    private final Configuration configuration;
    private final MappedStatement mappedStatement;
    private final RowBounds rowBounds;
    private final ResultHandler resultHandler;
    private final BoundSql boundSql;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final ObjectFactory objectFactory;

    /**
     * 没有值
     */
    private static final Object NO_VALUE = new Object();

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.rowBounds = rowBounds;
        this.boundSql = boundSql;
        this.mappedStatement = mappedStatement;
        this.resultHandler = resultHandler;
        this.objectFactory = configuration.getObjectFactory();
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    }

    /**
     * 处理多个结果集，并返回结果对象列表。
     *
     * @param statement Statement 对象
     * @return 包含多个结果对象的列表
     * @throws SQLException 如果处理结果集时发生 SQL 异常
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Object> handleResultSets(Statement statement) throws SQLException {
        // 存储多个结果对象的列表
        final List<Object> multipleResults = new ArrayList<>();

        // 结果集计数器
        int resultSetCount = 0;
        // 获取第一个结果集的 ResultSetWrapper 对象
        ResultSetWrapper wrapper = new ResultSetWrapper(statement.getResultSet(), configuration);

        // 获取语句中的 ResultMap 列表
        List<ResultMap> resultMaps = mappedStatement.getResultMaps();
        // 遍历结果集和对应的 ResultMap
        while (wrapper != null && resultMaps.size() > resultSetCount) {
            // 获取当前结果集对应的 ResultMap
            ResultMap resultMap = resultMaps.get(resultSetCount);
            // 处理当前结果集，并将结果添加到 multipleResults 中
            handleResultSet(wrapper, resultMap, multipleResults, null);
            // 获取下一个结果集的 ResultSetWrapper 对象
            wrapper = getNextResultSet(statement);
            // 结果集计数器递增
            resultSetCount++;
        }

        // 如果 multipleResults 中只有一个结果对象，直接返回该对象；否则返回 multipleResults 列表
        return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
    }


    /**
     * 获取下一个结果集包装对象。
     *
     * @param statement Statement 对象
     * @return 如果存在下一个结果集，则返回相应的 ResultSetWrapper 对象；否则返回 null
     */
    private ResultSetWrapper getNextResultSet(Statement statement) {
        try {
            // 检查数据库是否支持多结果集
            if (statement.getConnection().getMetaData().supportsMultipleResultSets() && (statement.getMoreResults() || statement.getUpdateCount() != -1)) {
                    ResultSet rs = statement.getResultSet();
                    // 如果结果集不为 null，则返回对应的 ResultSetWrapper 对象
                    return rs != null ? new ResultSetWrapper(rs, configuration) : null;

            }
        } catch (Exception ignore) {
            // 忽略异常，直接返回 null
        }
        // 如果出现异常或不存在下一个结果集，返回 null
        return null;
    }


    /**
     * 处理结果集
     *
     * @param wrapper         包装器
     * @param resultMap       结果集
     * @param multipleResults 多个结果
     * @param parentMapping   父映射
     */
    private void handleResultSet(ResultSetWrapper wrapper, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
        if(resultHandler == null) {
            // 创建新的结果处理器
            DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
            // 封装数据
            handleRowValuesForSimpleResultMap(wrapper,resultMap,defaultResultHandler,rowBounds,null);
            // 保存结果
            multipleResults.add(defaultResultHandler.getResultList());
        }
    }

    /**
     * 处理简单结果映射行值
     *
     * @param wrapper              包装器
     * @param resultMap            结果图
     * @param resultHandler 结果处理器
     * @param rowBounds            行范围
     * @param parentMapping        父映射
     */
    private void handleRowValuesForSimpleResultMap(ResultSetWrapper wrapper, ResultMap resultMap, ResultHandler resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
        DefaultResultContext resultContext = new DefaultResultContext();
        while(resultContext.getResultCount() < rowBounds.getLimit() && wrapper.getResultSet().next()) {
            Object rowValue = getRowValue(wrapper,resultMap);
            callResultHandler(resultHandler,resultContext,rowValue);
        }
    }

    /**
     * 调用结果处理器
     *
     * @param resultHandler 结果处理程序
     * @param resultContext 结果上下文
     * @param rowValue      行值
     */
    private void callResultHandler(ResultHandler resultHandler, DefaultResultContext resultContext, Object rowValue) {
        resultContext.nextResultObject(rowValue);
        resultHandler.handeResult(resultContext);
    }

    /**
     * 获取行值
     *
     * @param wrapper   包装器
     * @param resultMap 结果图
     * @return {@link Object}
     */
    private Object getRowValue(ResultSetWrapper wrapper, ResultMap resultMap) throws SQLException {
        // 根据返回类型，实例化对象
        Object resultObject = createResultObject(wrapper,resultMap,null);
        if(resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
            final MetaObject metaObject = configuration.newMetaObject(resultObject);
            // 自动映射 (将每列的值都赋在对应的字段上)
            applyAutomaticMappings(wrapper,resultMap,metaObject,null);
            // Map 映射 (根据映射类型赋值到字段)
            applyPropertyMappings(wrapper,resultMap,metaObject,null);
        }
        return resultObject;
    }

    /**
     * 应用属性映射
     *
     * @param wrapper      包装器
     * @param resultMap    结果图
     * @param metaObject   元对象
     * @param columnPrefix 列前缀
     * @return boolean
     */
    private boolean applyPropertyMappings(ResultSetWrapper wrapper, ResultMap resultMap, MetaObject metaObject, String columnPrefix) {
        boolean foundValues = false;
        try {
            final List<String> mappedColumnNames = wrapper.getMappedColumnNames(resultMap, columnPrefix);
            final List<ResultMapping> propertyMappings = resultMap.getResultMappings();
            for (ResultMapping propertyMapping : propertyMappings) {
                final String column = propertyMapping.getColumn();
                if(column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) {
                    final TypeHandler<?> typeHandler = propertyMapping.getTypeHandler();
                    Object value = typeHandler.getResult(wrapper.getResultSet(), column);
                    final String property = propertyMapping.getProperty();
                    if(value != NO_VALUE && property != null && value != null) {
                        metaObject.setValue(property, value);
                        foundValues = true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return foundValues;
    }

    /**
     * 应用自动映射将未映射的列数据设置到结果对象中。
     *
     * @param wrapper       结果集包装对象
     * @param resultMap     结果映射对象
     * @param metaObject    结果对象的元对象
     * @param columnPrefix  列名前缀
     * @return 如果找到任何未映射的列并成功设置到结果对象中，则返回 true；否则返回 false
     * @throws SQLException 如果 SQL 操作发生异常
     */
    private boolean applyAutomaticMappings(ResultSetWrapper wrapper, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
        // 获取未映射的列名列表
        final List<String> unMappedColumnNames = wrapper.getUnMappedColumnNames(resultMap, columnPrefix);

        // 标记是否找到任何未映射的列
        boolean foundValues = false;

        // 遍历未映射的列名列表
        for (String columnName : unMappedColumnNames) {
            String propertyName = columnName;

            // 如果存在列名前缀，且列名以列名前缀开头，则去掉前缀部分
            if (columnPrefix != null && !columnPrefix.isEmpty()) {
                if (columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
                    propertyName = columnName.substring(columnPrefix.length());
                } else {
                    continue; // 列名不以指定前缀开头，则跳过
                }
            }

            // 查找元对象中对应的属性名
            final String property = metaObject.findProperty(propertyName, false);

            // 如果属性名存在且具有对应的 setter 方法
            if (property != null && metaObject.hasSetter(property)) {
                // 获取属性的类型
                final Class<?> propertyType = metaObject.getSetterType(property);

                // 如果类型处理器中存在对应类型的处理器
                if (typeHandlerRegistry.hasTypeHandler(propertyType)) {
                    // 获取对应类型的类型处理器
                    final TypeHandler<?> typeHandler = wrapper.getTypeHandler(propertyType, columnName);

                    // 获取结果集中指定列的值
                    final Object value = typeHandler.getResult(wrapper.getResultSet(), columnName);

                    // 如果值不为 null，则表示找到了未映射的列
                    if (value != null) {
                        foundValues = true;
                    }

                    // 如果值不为 null，或者属性类型不是基本类型，则将值设置到结果对象的属性中
                    if (value != null || !propertyType.isPrimitive()) {
                        metaObject.setValue(property, value);
                    }
                }
            }
        }

        // 返回是否找到了未映射的列
        return foundValues;
    }


    /**
     * 创建结果对象
     *
     * @param wrapper      包装器
     * @param resultMap    结果图
     * @param columnPrefix 列前缀
     * @return {@link Object}
     */
    private Object createResultObject(ResultSetWrapper wrapper, ResultMap resultMap, String columnPrefix) throws SQLException {
        final List<Class<?>> constructorArgTypes = new ArrayList<>();
        final List<Object> constructorArgs = new ArrayList<>();
        return createResultObject(wrapper,resultMap,constructorArgTypes,constructorArgs,columnPrefix);
    }

    private Object createResultObject(ResultSetWrapper wrapper, ResultMap resultMap, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix) throws SQLException {
        final Class<?> resultType = resultMap.getType();
        final MetaClass metaType = MetaClass.forClass(resultType);
        if (typeHandlerRegistry.hasTypeHandler(resultType)) {
            // 基本类型
            return createPrimitiveResultObject(wrapper, resultMap, columnPrefix);
        } else if (resultType.isInterface() || metaType.hasDefaultConstructor()) {
            // 普通的Bean对象类型
            return objectFactory.create(resultType);
        }
        throw new RuntimeException("不知道如何创建 " + resultType + " 类型的实例。");
    }

    /**
     * 创建原语结果对象
     *
     * @param wrapper      包装器
     * @param resultMap    结果图
     * @param columnPrefix 列前缀
     * @return {@link Object}
     * @throws SQLException sqlexception异常
     */
    private Object createPrimitiveResultObject(ResultSetWrapper wrapper,ResultMap resultMap,String columnPrefix) throws SQLException {
        final Class<?> resultType = resultMap.getType();
        final String columnName;
        if(!resultMap.getResultMappings().isEmpty()) {
            final List<ResultMapping> resultMappingList = resultMap.getResultMappings();
            final ResultMapping mapping = resultMappingList.get(0);
            columnName = prependPrefix(mapping.getColumn(),columnPrefix);
        } else {
            columnName = wrapper.getColumnNames().get(0);
        }
        final TypeHandler<?> typeHandler = wrapper.getTypeHandler(resultType,columnName);
        return typeHandler.getResult(wrapper.getResultSet(),columnName);
    }

    /**
     * 预先考虑前缀
     *
     * @param columnName       列
     * @param columnPrefix 列前缀
     * @return {@link String}
     */
    private String prependPrefix(String columnName, String columnPrefix) {
        if(columnName == null || columnName.length() == 0 || columnPrefix == null || columnPrefix.length() == 0) {
            return columnName;
        }
        return columnPrefix + columnName;
    }
}
