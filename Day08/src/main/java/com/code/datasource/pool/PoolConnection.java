package com.code.datasource.pool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 池化连接
 *
 * @author HeXin
 * @date 2024/01/27
 */
public class PoolConnection implements InvocationHandler {

    /**
     * 关闭
     */
    private static final String CLOSE = "close";

    private static final Class<?>[] IFACES = new Class<?>[]{Connection.class};

    /**
     * 哈希码
     */
    private int hashCode = 0;

    /**
     * 数据源
     */
    private final PoolDataSource dataSource;

    /**
     * 真实的连接
     */
    private final Connection realConnection;

    /**
     * 代理连接
     */
    private final Connection proxyConnection;

    /**
     * 连接检查时间戳
     */
    private long checkoutTimestamp;

    /**
     * 创建时间戳
     */
    private long createdTimestamp;

    /**
     * 最近使用时间戳
     */
    private long lastUsedTimestamp;

    /**
     * 连接类型代码
     */
    private int connectionTypeCode;

    /**
     * 连接是否有效
     */
    private boolean valid;

    public PoolConnection(Connection connection, PoolDataSource dataSource){
        this.hashCode = connection.hashCode();
        this.realConnection = connection;
        this.dataSource = dataSource;
        this.createdTimestamp = System.currentTimeMillis();
        this.lastUsedTimestamp = System.currentTimeMillis();
        this.valid = true;
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),IFACES,this);
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        // 若调用 close 关闭链接方法，则将链接加入至连接池中，并返回null
        if(CLOSE.hashCode() == methodName.hashCode() && CLOSE.equals(methodName)){
            dataSource.pushConnection(this);
            return null;
        } else {
            // 否则执行原来的逻辑
            if(!Object.class.equals(method.getDeclaringClass())){
                // 除 toString() 方法，其他方法调用之前要检查 connection 是否合法
                checkConnection();
            }
        }
        // 其余方法则交给 connection 去调用
        return method.invoke(realConnection,args);
    }

    /**
     * 检查连接是否合法
     */
    private void checkConnection() throws SQLException {
        if(!valid){
            throw new SQLException("访问数据库连接池错误，连接非法！");
        }
    }

    public void invalidate(){
        valid = false;
    }

    public boolean isValid(){
        return valid && realConnection != null && dataSource.pingConnection(this);
    }

    public Connection getRealConnection() {
        return realConnection;
    }

    public Connection getProxyConnection() {
        return proxyConnection;
    }

    public int getRealHashCode(){
        return realConnection == null ? 0 : realConnection.hashCode();
    }

    public int getConnectionTypeCode() {
        return connectionTypeCode;
    }

    public void setConnectionTypeCode(int connectionTypeCode) {
        this.connectionTypeCode = connectionTypeCode;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getLastUsedTimestamp() {
        return lastUsedTimestamp;
    }

    public void setLastUsedTimestamp(long lastUsedTimestamp) {
        this.lastUsedTimestamp = lastUsedTimestamp;
    }

    /**
     * 获取自上次使用以来经过时间
     *
     * @return long
     */
    public long getTimeElapsedSinceLastUse() {
        return System.currentTimeMillis() - lastUsedTimestamp;
    }

    /**
     * 获取连接已创建时长
     *
     * @return long
     */
    public long getAge() {
        return System.currentTimeMillis() - createdTimestamp;
    }

    public long getCheckoutTimestamp() {
        return checkoutTimestamp;
    }

    public void setCheckoutTimestamp(long timestamp) {
        this.checkoutTimestamp = timestamp;
    }

    /**
     * 获取已签出时间
     *
     * @return long
     */
    public long getCheckoutTime() {
        return System.currentTimeMillis() - checkoutTimestamp;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PoolConnection) {
            return realConnection.hashCode() == (((PoolConnection) obj).realConnection.hashCode());
        } else if (obj instanceof Connection) {
            return hashCode == obj.hashCode();
        } else {
            return false;
        }
    }

}
