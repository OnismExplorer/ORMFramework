package com.code.script.xmltags;

import com.code.mapping.SqlSource;
import com.code.script.LanguageDriver;
import com.code.session.Configuration;
import org.dom4j.Element;

/**
 * XML 语言驱动器
 *
 * @author HeXin
 * @date 2024/02/07
 */
public class XMLLanguageDriver implements LanguageDriver {
    @Override
    public SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType) {
        // 使用 XML 脚本构建器解析
        XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }
}
