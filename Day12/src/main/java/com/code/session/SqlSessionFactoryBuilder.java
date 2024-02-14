package com.code.session;

import com.code.builder.xml.XMLConfigBuilder;
import com.code.session.defaults.DefaultSqlSessionFactory;

import java.io.Reader;

/**
 * SqlSessionFactory 构建器
 *
 * @author HeXin
 * @date 2024/01/25
 */
public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(Reader reader) {
        XMLConfigBuilder builder = new XMLConfigBuilder(reader);
        return build(builder.parse());
    }

    public SqlSessionFactory build(Configuration configuration) {
        return new DefaultSqlSessionFactory(configuration);
    }
}
