package com.code.datasource.pool;

import com.code.datasource.unpool.UnpoolDataSourceFactory;

import javax.sql.DataSource;

/**
 * 连接池数据源工厂
 *
 * @author HeXin
 * @date 2024/01/29
 */
public class PoolDataSourceFactory extends UnpoolDataSourceFactory {

    public PoolDataSourceFactory() {
        this.dataSource = new PoolDataSource();
    }
}
