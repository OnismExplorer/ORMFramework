package com.code.script;

import com.code.executor.parameter.ParameterHandler;
import com.code.mapping.BoundSql;
import com.code.mapping.MappedStatement;
import com.code.mapping.SqlSource;
import com.code.session.Configuration;
import org.dom4j.Element;


/**
 * 脚本语言驱动
 *
 * @author HeXin
 * @date 2024/02/07
 */
public interface LanguageDriver {
    /**
     * 创建SQL源代码
     *
     * @param configuration 配置
     * @param script        脚本
     * @param parameterType 参数类型
     * @return {@link SqlSource}
     */
    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);

    /**
     * 创建参数处理器
     *
     * @param mappedStatement 映射语句
     * @param parameterObject 参数对象
     * @param boundSql        绑定sql
     * @return {@link ParameterHandler}
     */
    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);
}
