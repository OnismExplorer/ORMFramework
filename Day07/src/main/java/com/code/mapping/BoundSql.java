package com.code.mapping;

import java.util.Map;

/**
 * 绑定的SQL，从SqlSource而来，将动态内容处理完成得到的SQL语句，其中包括?以及绑定的参数
 *
 * @author HeXin
 * @date 2024/01/26
 */
public record BoundSql(String sql, Map<Integer, String> parameterMappings, String parameterType, String resultType) {

}
