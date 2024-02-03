package com.code.datasource.unpool;

import com.code.datasource.DataSourceFactory;
import com.code.reflection.MetaObject;
import com.code.reflection.SystemMetaObject;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 无池化数据源工厂
 *
 * @author HeXin
 * @date 2024/01/30
 */
public class UnpoolDataSourceFactory implements DataSourceFactory {

    protected DataSource dataSource;

    public UnpoolDataSourceFactory() {
        this.dataSource = new UnpoolDataSource();
    }

    @Override
    public void setProperties(Properties props) {
        MetaObject metaObject = SystemMetaObject.forObject(dataSource);
        for (Object key : props.keySet()) {
            String propertyName = (String) key;
            if (metaObject.hasSetter(propertyName)) {
                String value = (String) props.get(propertyName);
                Object convertValue = convertValue(metaObject, propertyName, value);
                metaObject.setValue(propertyName, convertValue);
            }
        }
    }

    private Object convertValue(MetaObject metaObject, String propertyName, String value) {
        Object convertValue = value;
        Class<?> targetType = metaObject.getSetterType(propertyName);
        if (targetType == Integer.class || targetType == int.class) {
            convertValue = Integer.valueOf(value);
        } else if (targetType == Long.class || targetType == long.class) {
            convertValue = Long.valueOf(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            convertValue = Boolean.valueOf(value);
        }
        return convertValue;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
