package com.code.mapping;

import com.code.executor.keygen.Jdbc3KeyGenerator;
import com.code.executor.keygen.KeyGenerator;
import com.code.executor.keygen.NoKeyGenerator;
import com.code.script.LanguageDriver;
import com.code.session.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 映射语句类
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class MappedStatement {
    private Configuration configuration;
    private String id;
    private SqlCommandType sqlCommandType;

    private SqlSource sqlSource;
    Class<?> resultType;

    /**
     * 脚本语言驱动
     */
    private LanguageDriver languageDriver;

    private List<ResultMap> resultMaps;

    private String resource;

    /**
     * 键生成器
     */
    private KeyGenerator keyGenerator;

    /**
     * 关键属性
     */
    private String[] keyProperties;

    /**
     * 键列
     */
    private String[] keyColumns;

    /**
     * 是否需要刷新缓存
     */
    private boolean flushCacheRequired;


    /**
     * 默认构造器
     */
    MappedStatement () {
    }

    /**
     * 建造者
     *
     * @author HeXin
     * @date 2024/01/25
     */
    public static class Builder {
        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration,String id,SqlCommandType sqlCommandType,SqlSource sqlSource,Class<?> resultType){
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.resultType = resultType;
            mappedStatement.languageDriver = configuration.getDefaultScriptLanguageInstance();
            mappedStatement.keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
        }

        public MappedStatement build(){
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            mappedStatement.resultMaps = Collections.unmodifiableList(mappedStatement.resultMaps);
            return mappedStatement;
        }

        public String id() {
            return mappedStatement.id;
        }

        public Builder resultMaps(List<ResultMap> resultMaps) {
            mappedStatement.resultMaps = resultMaps;
            return this;
        }

        public Builder resource(String resource) {
            mappedStatement.resource = resource;
            return this;
        }

        public Builder keyGenerator(KeyGenerator keyGenerator) {
            mappedStatement.keyGenerator = keyGenerator;
            return this;
        }

        public Builder keyProperty(String keyProperty) {
            mappedStatement.keyProperties = delimitedStringToArray(keyProperty);
            return this;
        }
    }

    /**
     * 分隔字符串到数组
     *
     * @param str str
     * @return {@link String[]}
     */
    private static String[] delimitedStringToArray(String str) {
        if(str == null || str.trim().length() == 0) {
            return null;
        }
        return str.split(",");
    }

    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }
    public Configuration getConfiguration() {
        return configuration;
    }

    public String getId() {
        return id;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public SqlSource getSqlSource() {
        return sqlSource;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public LanguageDriver getLanguageDriver() {
        return languageDriver;
    }

    public List<ResultMap> getResultMaps() {
        return resultMaps;
    }

    public String getResource() {
        return resource;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public String[] getKeyProperties() {
        return keyProperties;
    }

    public String[] getKeyColumns() {
        return keyColumns;
    }

    public boolean isFlushCacheRequired() {
        return flushCacheRequired;
    }
}
