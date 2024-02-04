package com.code.datasource.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.code.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 德鲁伊数据源工厂
 *
 * @author HeXin
 * @date 2024/01/26
 */
public class DruidDataSourceFactory implements DataSourceFactory {
    private Properties properties;

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public DataSource getDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(properties.getProperty("driver"));
        dataSource.setUrl(properties.getProperty("url"));
        dataSource.setUsername(properties.getProperty("username"));
        dataSource.setPassword(properties.getProperty("password"));
        return dataSource;
    }

}
