package com.code.builder;

import com.code.session.Configuration;

public abstract class BaseBuilder {
   protected final Configuration configuration;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
