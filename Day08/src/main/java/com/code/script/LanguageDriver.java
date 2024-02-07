package com.code.script;

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
    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);
}
