package com.code.mapping;

import com.code.transaction.TransactionFactory;

import javax.sql.DataSource;

/**
 * 环境类
 *
 * @param id                 id
 * @param transactionFactory 事务工厂
 * @param dataSource         数据源
 * @author HeXin
 * @date 2024/01/26
 */
public record Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {

    public static class Builder {

        private final String id;
        private TransactionFactory transactionFactory;
        private DataSource dataSource;

        public Builder(String id) {
            this.id = id;
        }

        public Builder transactionFactory(TransactionFactory transactionFactory) {
            this.transactionFactory = transactionFactory;
            return this;
        }

        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public String id() {
            return this.id;
        }

        public Environment build() {
            return new Environment(this.id, this.transactionFactory, this.dataSource);
        }

    }


}
