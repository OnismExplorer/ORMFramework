package com.code.builder;

import com.code.session.Configuration;
import com.code.type.TypeAliasRegistry;
import com.code.type.TypeHandlerRegistry;

/**
 * 基础建设者
 *
 * @author HeXin
 * @date 2024/01/26
 */
public abstract class BaseBuilder {
   protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;
    protected final TypeHandlerRegistry typeHandlerRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 解析别名
     *
     * @param alias 别名
     * @return {@link Class}<{@link ?}>
     */
    protected Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }
}
