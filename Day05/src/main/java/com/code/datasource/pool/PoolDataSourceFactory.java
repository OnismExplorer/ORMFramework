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
    /**
     * 获取数据源
     *
     * @return {@link DataSource}
     */
    @Override
    public DataSource getDataSource() {
        PoolDataSource dataSource = new PoolDataSource();
        dataSource.setDriver(properties.getProperty("driver"));
        dataSource.setUrl(properties.getProperty("url"));
        dataSource.setUsername(properties.getProperty("username"));
        dataSource.setPassword(properties.getProperty("password"));
        return dataSource;
    }
}
