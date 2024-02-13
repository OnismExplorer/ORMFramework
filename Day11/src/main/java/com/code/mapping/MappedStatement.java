package com.code.mapping;

import com.code.script.LanguageDriver;
import com.code.session.Configuration;

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
        }

        public MappedStatement build(){
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            return mappedStatement;
        }

        public String id() {
            return mappedStatement.id;
        }

        public Builder resultMaps(List<ResultMap> resultMaps) {
            mappedStatement.resultMaps = resultMaps;
            return this;
        }
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
}
