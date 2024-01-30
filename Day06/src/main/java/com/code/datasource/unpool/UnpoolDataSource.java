package com.code.datasource.unpool;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 无池化数据源
 *
 * @author HeXin
 * @date 2024/01/27
 */
public class UnpoolDataSource implements DataSource {

    /**
     * 驱动注册器
     */
    private static final Map<String, Driver> REGISTERED_DRIVERS = new ConcurrentHashMap<>();

    static {
        // 获取已注册的 JDBC 驱动程序的 Enumeration 枚举迭代器对象
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            // 获取下一个 JDBC 驱动程序
            Driver driver = drivers.nextElement();
            REGISTERED_DRIVERS.put(driver.getClass().getName(), driver);
        }
    }

    private ClassLoader driverClassLoader;
    /**
     * 驱动配置，可以扩展属性信息，如driver.encoding=UTF8
     */
    private Properties driverProperties;
    /**
     * 驱动
     */
    private String driver;
    /**
     * url地址
     */
    private String url;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 是否自动提交
     */
    private Boolean autoCommit;
    /**
     * 事务隔离级别
     */
    private Integer defaultTransactionIsolationLevel;

    /**
     * 初始化驱动
     *
     * @throws SQLException SQLException
     */
    private synchronized void initializerDriver() throws SQLException {
        if (!REGISTERED_DRIVERS.containsKey(driver)) {
            try {
                Class<?> driverType = Class.forName(driver, true, driverClassLoader);
                Driver driverInstance = (Driver) driverType.getDeclaredConstructor().newInstance();
                // 注册驱动
                DriverManager.registerDriver(new DriverProxy(driverInstance));
                REGISTERED_DRIVERS.put(driver, driverInstance);
            } catch (Exception e) {
                throw new SQLException("无池化数据源驱动设置失败，失败原因：" + e);
            }
        }
    }

    /**
     * 通过用户名与密码获取连接
     *
     * @param username 用户名
     * @param password 密码
     * @return {@link Connection}
     * @throws SQLException SQLException
     */
    private Connection doGetConnection(String username, String password) throws SQLException {
        Properties properties = new Properties();
        if (driverProperties != null) {
            properties.putAll(driverProperties);
        }
        if (username != null) {
            // 设置用户
            properties.setProperty("user", username);
        }
        if (password != null) {
            properties.setProperty("password", password);
        }
        return doGetConnection(properties);
    }

    /**
     * 通过 Properties 配置获取连接
     *
     * @param properties 配置
     * @return {@link Connection}
     * @throws SQLException SQLException
     */
    private Connection doGetConnection(Properties properties) throws SQLException {
        initializerDriver();
        Connection connection = DriverManager.getConnection(url, properties);
        if (autoCommit != null && autoCommit != connection.getAutoCommit()) {
            connection.setAutoCommit(autoCommit);
        }
        if (defaultTransactionIsolationLevel != null) {
            connection.setTransactionIsolation(defaultTransactionIsolationLevel);
        }
        return connection;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        DriverManager.setLogWriter(out);
    }

    @Override
    public int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public void setLoginTimeout(int seconds) {
        DriverManager.setLoginTimeout(seconds);
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " 并不是一个wrapper映射");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }

    public ClassLoader getDriverClassLoader() {
        return driverClassLoader;
    }

    public void setDriverClassLoader(ClassLoader driverClassLoader) {
        this.driverClassLoader = driverClassLoader;
    }

    public Properties getDriverProperties() {
        return driverProperties;
    }

    public void setDriverProperties(Properties driverProperties) {
        this.driverProperties = driverProperties;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public Integer getDefaultTransactionIsolationLevel() {
        return defaultTransactionIsolationLevel;
    }

    public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
        this.defaultTransactionIsolationLevel = defaultTransactionIsolationLevel;
    }

    /**
         * 驱动程序代理
         *
         * @author HeXin
         * @date 2024/01/27
         */
        private record DriverProxy(Driver driver) implements Driver {

        /**
             * 连接
             *
             * @param url  网址
             * @param info 信息
             * @return {@link Connection}
             * @throws SQLException SQLException
             */
            @Override
            public Connection connect(String url, Properties info) throws SQLException {
                return this.driver.connect(url, info);
            }

            /**
             * 配置 URL
             *
             * @param url 网址
             * @return boolean
             * @throws SQLException SQLException
             */
            @Override
            public boolean acceptsURL(String url) throws SQLException {
                return this.driver.acceptsURL(url);
            }

            /**
             * 获取配置信息
             *
             * @param url  网址
             * @param info 信息
             * @return {@link DriverPropertyInfo[]}
             * @throws SQLException SQLException
             */
            @Override
            public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
                return this.driver.getPropertyInfo(url, info);
            }

            /**
             * 获取主要版本
             *
             * @return int
             */
            @Override
            public int getMajorVersion() {
                return this.driver.getMajorVersion();
            }

            /**
             * 获取次要版本
             *
             * @return int
             */
            @Override
            public int getMinorVersion() {
                return this.driver.getMinorVersion();
            }

            /**
             * 符合JDBC标准
             *
             * @return boolean
             */
            @Override
            public boolean jdbcCompliant() {
                return this.driver.jdbcCompliant();
            }

            /**
             * 获取父记录器
             *
             * @return {@link Logger}
             */
            @Override
            public Logger getParentLogger() {
                return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            }
        }
}
