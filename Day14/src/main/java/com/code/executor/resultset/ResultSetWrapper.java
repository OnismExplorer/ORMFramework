package com.code.executor.resultset;

import cn.hutool.core.collection.CollUtil;
import com.code.io.Resources;
import com.code.mapping.ResultMap;
import com.code.session.Configuration;
import com.code.type.JdbcType;
import com.code.type.TypeHandler;
import com.code.type.TypeHandlerRegistry;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * 结果集包装器
 *
 * @author HeXin
 * @date 2024/02/12
 */
public class ResultSetWrapper {
    /**
     * 结果集
     */
    private final ResultSet resultSet;
    /**
     * 类型处理程序注册表
     */
    private final TypeHandlerRegistry typeHandlerRegistry;

    /**
     * 列名
     */
    private final List<String> columnNames = new ArrayList<>();

    /**
     * 类名
     */
    private final List<String> classNames = new ArrayList<>();

    /**
     * jdbc类型
     */
    private final List<JdbcType> jdbcTypes = new ArrayList<>();

    /**
     * 类型处理程序映射
     */
    private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();

    /**
     * 映射列名映射
     */
    private Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();


    /**
     * 非映射列名映射
     */
    private Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();

    public ResultSetWrapper(ResultSet resultSet, Configuration configuration) throws SQLException {
        super();
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.resultSet = resultSet;
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnLabel(i));
            jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i)));
            classNames.add(metaData.getColumnClassName(i));
        }
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<String> getClassNames() {
        return Collections.unmodifiableList(classNames);
    }

    /**
     * 获取类型处理器
     *
     * @param propertyType 属性类型
     * @param columnName   列名
     * @return {@link TypeHandler}<{@link ?}>
     */
    public TypeHandler<?> getTypeHandler(Class<?> propertyType, String columnName) {
        TypeHandler<?> handler = null;
        Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.get(columnName);
        if (CollUtil.isEmpty(columnHandlers)) {
            columnHandlers = new HashMap<>();
            typeHandlerMap.put(columnName, columnHandlers);
        } else {
            handler = columnHandlers.get(propertyType);
        }
        if (handler == null) {
            handler = typeHandlerRegistry.getTypeHandler(propertyType, null);
            columnHandlers.put(propertyType, handler);
        }
        return handler;
    }

    /**
     * 拆分类
     *
     * @param className 类名
     * @return {@link Class}<{@link ?}>
     */
    public Class<?> resolveClass(String className) {
        return Resources.classForName(className);
    }

    /**
     * 加载映射和未映射的列名
     *
     * @param resultMap    结果映射对象
     * @param columnPrefix 列名前缀
     * @throws SQLException 如果 SQL 操作发生异常
     */
    private void loadMappedAndUnmappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
        // 存储已映射的列名和未映射的列名
        List<String> mappedColumnNames = new ArrayList<>();
        List<String> unmappedColumnNames = new ArrayList<>();

        // 转换列名前缀为大写
        final String upperColumnPrefix = columnPrefix == null ? null : columnPrefix.toUpperCase(Locale.ENGLISH);

        // 获取已映射的列名集合
        final Set<String> mappedColumns = prependPrefixes(resultMap.getMappedColumns(), upperColumnPrefix);

        // 遍历所有列名
        for (String columnName : columnNames) {
            // 转换列名为大写
            final String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);

            // 判断列名是否在已映射的列名集合中
            if (mappedColumns.contains(upperColumnName)) {
                // 如果在，则添加到已映射的列名列表
                mappedColumnNames.add(upperColumnName);
            } else {
                // 否则，添加到未映射的列名列表
                unmappedColumnNames.add(columnName);
            }
        }

        // 将结果存储到映射的列名和未映射的列名的 Map 中
        mappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), mappedColumnNames);
        unMappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), unmappedColumnNames);
    }

    private Set<String> prependPrefixes(Set<String> columnNames, String columnPrefix){
        if(CollUtil.isEmpty(columnNames) || columnPrefix == null || columnPrefix.length() == 0) {
            return  columnNames;
        }
        final Set<String> prefixed = new HashSet<>();
        for (String columnName : columnNames) {
            prefixed.add(columnPrefix + columnName);
        }
        return prefixed;
    }

    /**
     * 获取 Map 键
     *
     * @param resultMap    结果集
     * @param columnPrefix 列前缀
     * @return {@link String}
     */
    private String getMapKey(ResultMap resultMap,String columnPrefix) {
        return resultMap.getId() + ":" +columnPrefix;
    }

    /**
     * 获取映射列名
     *
     * @param resultMap    结果图
     * @param columnPrefix 列前缀
     * @return {@link List}<{@link String}>
     * @throws SQLException sqlexception异常
     */
    public List<String> getMappedColumnNames(ResultMap resultMap,String columnPrefix) throws SQLException {
        List<String> mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        if(CollUtil.isEmpty(mappedColumnNames)) {
            loadMappedAndUnmappedColumnNames(resultMap,columnPrefix);
            mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap,columnPrefix));
        }
        return mappedColumnNames;
    }

    /**
     * 获取未映射列名
     *
     * @param resultMap    结果图
     * @param columnPrefix 列前缀
     * @return {@link List}<{@link String}>
     * @throws SQLException sqlexception异常
     */
    public List<String> getUnMappedColumnNames(ResultMap resultMap,String columnPrefix) throws SQLException {
        List<String> unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        if(CollUtil.isEmpty(unMappedColumnNames)) {
            loadMappedAndUnmappedColumnNames(resultMap,columnPrefix);
            unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap,columnPrefix));
        }
        return unMappedColumnNames;
    }
}
