package com.code.mapping;

import com.code.session.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 结果映射
 *
 * @author HeXin
 * @date 2024/02/12
 */
public class ResultMap {

    private String id;

    private Class<?> type;

    /**
     * 结果映射
     */
    private List<ResultMapping> resultMappings;

    /**
     * 映射列
     */
    private Set<String> mappedColumns;

    public ResultMap() {
    }

    public static class Builder {
        /**
         * 结果映射
         */
        private ResultMap resultMap = new ResultMap();

        public Builder(Configuration configuration,String id,Class<?> type,List<ResultMapping> resultMappings) {
            resultMap.id = id;
            resultMap.type = type;
            resultMap.resultMappings = resultMappings;
        }

        public ResultMap build() {
            resultMap.mappedColumns = new HashSet<>();
            return resultMap;
        }
    }

    public String getId() {
        return id;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ResultMapping> getResultMappings() {
        return resultMappings;
    }

    public Set<String> getMappedColumns() {
        return mappedColumns;
    }
}
