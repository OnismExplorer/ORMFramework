package com.code.builder;

import com.code.session.Configuration;
import com.code.type.TypeAliasRegistry;

/**
 * 基础建设者
 *
 * @author HeXin
 * @date 2024/01/26
 */
public abstract class BaseBuilder {
   protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;

    protected BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
