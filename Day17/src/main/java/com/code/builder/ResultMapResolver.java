package com.code.builder;

import com.code.mapping.ResultMap;
import com.code.mapping.ResultMapping;

import java.util.List;

/**
 * 结果映射解析器
 *
 * @author HeXin
 * @date 2024/02/16
 */
public class ResultMapResolver {
    private final MapperBuilderAssistant assistant;

    private String id;

    private Class<?> type;

    private List<ResultMapping> resultMappings;

    public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, List<ResultMapping> resultMappings) {
        this.assistant = assistant;
        this.id = id;
        this.type = type;
        this.resultMappings = resultMappings;
    }

    public ResultMap resolve() {
        return assistant.addResultMap(this.id,this.type,this.resultMappings);
    }
}
