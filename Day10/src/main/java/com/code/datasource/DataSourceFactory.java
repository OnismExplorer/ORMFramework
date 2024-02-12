package com.code.datasource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 数据源工厂
 *
 * @author HeXin
 * @date 2024/01/26
 */
public interface DataSourceFactory {
    void setProperties(Properties props);

    DataSource getDataSource();

}
