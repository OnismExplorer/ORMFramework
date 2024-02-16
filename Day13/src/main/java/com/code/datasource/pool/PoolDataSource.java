package com.code.datasource.pool;



import com.code.datasource.unpool.UnpoolDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.logging.Logger;

/**
 * 有连接池的数据源
 * @author HeXin
 * @date 2024/01/27
 */
public class PoolDataSource implements DataSource {

    /**
     * 池状态
     */
    private final PoolState poolState =  new PoolState(this);

    /**
     * 数据源
     */
    private final UnpoolDataSource dataSource;

    /**
     * 最大活跃连接数
     */
    protected int poolMaximumActiveConnections = 10;

    /**
     * 最大空闲连接数
     */
    protected int poolMaximumIdleConnections = 5;

    /**
     * 池中连接被检查的时间(连接被强制返回之前)
     */
    protected int poolMaximumCheckOutTime = 15000;

    /**
     * 池等待时间(连接池打印日志状态，并重新尝试获取连接，避免连接池没有配置时静默失败)。
     */
    protected int poolTimeToWait = 20000;

    /**
     * 池 ping 查询，杨宏宇验证连接是否正常工作，并准备接受请求(默认为 "NO PING QUERY SET"，这会引起许多数据库驱动连接由一个错误的信息而导致失败)
     */
    protected String poolPingQuery = "NO PING QUERY SET";

    /**
     * 开启或禁用侦测查询
     */
    protected boolean poolPingEnabled = false;

    /**
     * 配置 poolPingQuery 的使用间隔
     */
    protected int poolPingConnectionsNotUseFor = 0;

    /**
     * 预期连接类型代码
     */
    private int expectedConnectionTypeCode;

    public PoolDataSource() {
        this.dataSource = new UnpoolDataSource();
    }

    /**
     * 收回连接
     *
     * @param connection 连接
     * @throws SQLException
     */
    protected void pushConnection(PoolConnection connection) throws SQLException {
        // 加锁，防止其余线程同时抢夺同一个线程
        synchronized (poolState) {
            // 将该连接从活跃线程中移除
            poolState.activeConnections.remove(connection);
            // 判断该连接是否有效
            if(connection.isValid()){
                poolState.accumulatedCheckOutTime += connection.getCheckoutTime();
                // 首先检查数据库是否处于自动提交模式，若不是则调用 rollback() 方法执行回滚操作(保证数据库的一致性)
                if(!connection.getRealConnection().getAutoCommit()){
                    connection.getRealConnection().rollback();
                }
                // 若空闲链接小于设定数量
                if(poolState.idleConnections.size() < poolMaximumIdleConnections && connection.getConnectionTypeCode() == expectedConnectionTypeCode){
                    // 实例化一个新的数据库连接，加入到 idle 列表
                    PoolConnection newConnection = new PoolConnection(connection.getRealConnection(), this);
                    poolState.idleConnections.add(newConnection);
                    newConnection.setCreatedTimestamp(connection.getCreatedTimestamp());
                    newConnection.setLastUsedTimestamp(connection.getLastUsedTimestamp());
                    System.out.println("归还连接 "+newConnection.getRealHashCode() +" 到连接池");

                    // 通知其余线程来获取数据库连接
                    poolState.notifyAll();
                } else { // 否则，空闲连接比较充足
                    // 将该连接关闭
                    connection.getRealConnection().close();
                    System.out.println(connection.getRealHashCode() + " 连接已关闭");
                    connection.invalidate();
                }
            } else {
                System.out.println("错误连接 "+connection.getRealHashCode()+" 尝试放回置连接池，已丢弃该连接！");
                poolState.badConnectionCount++;
            }
        }
    }

    /**
     * 弹出连接
     *
     * @param username 用户名
     * @param password 密码
     * @return {@link PoolConnection}
     * @throws SQLException SQLException
     */
    private PoolConnection popConnection(String username,String password) throws SQLException{
        boolean countedWait = false;
        PoolConnection connection = null;
        // 获取当前时间戳
        long stamp = System.currentTimeMillis();
        int localBadConnectionCount = 0;
        while(connection == null){
            synchronized (poolState) {
                // 若有空闲连接则返回第一个(出栈操作)
                if(!poolState.idleConnections.isEmpty()){
                    connection = poolState.idleConnections.remove(0);
                    System.out.println("已将 "+connection.getRealHashCode()+" 连接签出连接池");
                } else { // 若无空闲连接，则创建新的连接
                    // 活跃数未满
                    if(poolState.activeConnections.size() < poolMaximumActiveConnections) {
                        connection = new PoolConnection(dataSource.getConnection(), this);
                        System.out.println("新建连接："+connection.getRealHashCode());
                    } else { // 活跃连接数已满
                        // 获取活跃连接列表中的第一个，即最老的连接
                        PoolConnection oldestConnection = poolState.activeConnections.get(0);
                        long longestCheckOutTime = oldestConnection.getCheckoutTime();
                        // 若 checkout 时间过长，则该连接标记为过期
                        if(longestCheckOutTime > poolMaximumCheckOutTime){
                            poolState.claimedOverdueConnectionCount++;
                            poolState.accumulatedCheckOutTimeOfOverdueConnections += longestCheckOutTime;
                            poolState.accumulatedCheckOutTime += longestCheckOutTime;
                            poolState.activeConnections.remove(oldestConnection);
                            if(!oldestConnection.getRealConnection().getAutoCommit()){
                                oldestConnection.getRealConnection().rollback();
                            }
                            // 删除最老的连接，重新实例化一个新连接
                            connection = new PoolConnection(oldestConnection.getRealConnection(),this);
                            // 将旧连接置为非法连接
                            oldestConnection.invalidate();
                            System.out.println("已声明的逾期连接："+connection.getRealConnection());
                        } else { // 若超过时间不够长则继续等待
                            try {
                                if(!countedWait){
                                    poolState.hadToWaitCount++;
                                    countedWait = true;
                                }
                                System.out.println("连接等待 " + poolTimeToWait + " 毫秒");
                                long waitTime = System.currentTimeMillis();
                                poolState.wait(poolTimeToWait);
                                poolState.accumulatedWaitTime += System.currentTimeMillis() - waitTime;
                            } catch (InterruptedException e) {
                                System.err.println("请求连接时发生错误！");
                                /* Clean up whatever needs to be handled before interrupting  */
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                }
                // 获取到连接
                if(connection != null){
                    if(connection.isValid()){
                        if(!connection.getRealConnection().getAutoCommit()){
                            connection.getRealConnection().rollback();
                        }
                        connection.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(),username,password));
                        // 记录 check out 时间
                        connection.setCheckoutTimestamp(System.currentTimeMillis());
                        connection.setLastUsedTimestamp(System.currentTimeMillis());
                        poolState.activeConnections.add(connection);
                        poolState.requestCount++;
                        poolState.accumulatedRequestTime += System.currentTimeMillis() - stamp;
                    } else {
                        System.out.println("一个错误的连接 "+connection.getRealHashCode()+" 从连接池中返回，正在重新获得其他连接！");
                        // 若未拿到连接则统计失败连接数加一
                        poolState.badConnectionCount++;
                        localBadConnectionCount++;
                        connection = null;
                        // 如果失败次数较多则抛出异常(加三证明有三次试错机会)
                        if(localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
                            throw new SQLException("已无法从该数据源的线程池中获取正确连接！");
                        }
                    }
                }
            }
        }
        // 此时连接若为空则证明发生异常
        if(connection == null) {
            throw new SQLException("因发生未知的严重错误情况， 连接池返回了空连接！");
        }
        return connection;
    }

    /**
     * 强制关闭全部连接
     */
    public void forceCloseAll() {
        synchronized (poolState) {
            expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(),dataSource.getUsername(),dataSource.getPassword());
            // 关闭活跃连接
            for(int i = poolState.activeConnections.size() - 1;i >= 0;i--){
                try{
                    PoolConnection connection = poolState.activeConnections.remove(i);
                    connection.invalidate();

                    Connection realConnection = connection.getRealConnection();
                    if(!realConnection.getAutoCommit()){
                        realConnection.rollback();
                    }
                    realConnection.close();
                } catch (Exception ex){
                    System.err.println("关闭失败！");
                }
            }
            System.out.println("数据库连接池已强制关闭所有连接！");
        }
    }

    /**
     * ping 连接
     *
     * @param connection 连接
     * @return boolean
     */
    protected boolean pingConnection(PoolConnection connection) {
        boolean result;
        try {
            result = ! connection.getRealConnection().isClosed();
        } catch (SQLException ex){
            System.err.println(connection.getRealHashCode() + " 是错误连接，错误信息为："+ex.getMessage());
            result = false;
        }

        if(result && (poolPingEnabled && (poolPingConnectionsNotUseFor >= 0 && connection.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUseFor))){
                    try {
                        System.out.println("Ping connection "+connection.getRealHashCode()+" ...");
                        Connection realConnection = connection.getRealConnection();
                        Statement statement = realConnection.createStatement();
                        ResultSet resultSet = statement.executeQuery(poolPingQuery);
                        resultSet.close();
                        if(!realConnection.getAutoCommit()){
                            realConnection.rollback();
                        }
                        // result = true
                        System.out.println("Connection "+connection.getRealHashCode()+" is good!");
                    } catch (Exception e){
                        System.err.println("执行 Ping 请求 ' "+poolPingQuery+" '时发生错误，错误信息："+e.getMessage());
                        try {
                            connection.getRealConnection().close();
                        } catch (SQLException ex){
                            System.err.println("关闭连接时出错！");
                        }
                        result = false;
                        System.err.println(connection.getRealHashCode() + " 是错误连接，错误信息为："+e.getMessage());
                    }


        }
        return result;
    }

    /**
     * 解包连接
     *
     * @param connection 连接
     * @return {@link Connection}
     */
    public static Connection unwrapConnection(Connection connection) {
        if(Proxy.isProxyClass(connection.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(connection);
            if(handler instanceof PoolConnection) {
                return ((PoolConnection) handler).getRealConnection();
            }
        }
        return connection;
    }

    private int assembleConnectionTypeCode(String url,String username,String password) {
        return ("" +url + username + password).hashCode();
    }

    /**
     * 获取连接(数据源)
     *
     * @return {@link Connection}
     * @throws SQLException SQLException
     */
    @Override
    public Connection getConnection() throws SQLException {
        // 通过账号密码获取代理连接
        return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
    }

    /**
     * 获取连接(账号，密码)
     *
     * @param username 用户名
     * @param password 密码
     * @return {@link Connection}
     * @throws SQLException SQLException
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return popConnection(username,password).getProxyConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName()+"不是一个wrapper映射");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public void setDriver(String driver) {
        dataSource.setDriver(driver);
        forceCloseAll();
    }

    public void setUrl(String url) {
        dataSource.setUrl(url);
        forceCloseAll();
    }

    public void setUsername(String username) {
        dataSource.setUsername(username);
        forceCloseAll();
    }

    public void setPassword(String password) {
        dataSource.setPassword(password);
        forceCloseAll();
    }

    public String getDriver() {
        return dataSource.getDriver();
    }

    public String getUrl() {
        return dataSource.getUrl();
    }

    public String getUsername() {
        return dataSource.getUsername();
    }

    public String getPassword() {
        return dataSource.getPassword();
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        dataSource.setAutoCommit(defaultAutoCommit);
        forceCloseAll();
    }

    public int getPoolMaximumActiveConnections() {
        return poolMaximumActiveConnections;
    }

    public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
        this.poolMaximumActiveConnections = poolMaximumActiveConnections;
    }

    public int getPoolMaximumIdleConnections() {
        return poolMaximumIdleConnections;
    }

    public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
        this.poolMaximumIdleConnections = poolMaximumIdleConnections;
    }

    public int getPoolMaximumCheckoutTime() {
        return poolMaximumCheckOutTime;
    }

    public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
        this.poolMaximumCheckOutTime = poolMaximumCheckoutTime;
    }

    public int getPoolTimeToWait() {
        return poolTimeToWait;
    }

    public void setPoolTimeToWait(int poolTimeToWait) {
        this.poolTimeToWait = poolTimeToWait;
    }

    public String getPoolPingQuery() {
        return poolPingQuery;
    }

    public void setPoolPingQuery(String poolPingQuery) {
        this.poolPingQuery = poolPingQuery;
    }

    public boolean isPoolPingEnabled() {
        return poolPingEnabled;
    }

    public void setPoolPingEnabled(boolean poolPingEnabled) {
        this.poolPingEnabled = poolPingEnabled;
    }

    public int getPoolPingConnectionsNotUsedFor() {
        return poolPingConnectionsNotUseFor;
    }

    public void setPoolPingConnectionsNotUsedFor(int poolPingConnectionsNotUsedFor) {
        this.poolPingConnectionsNotUseFor = poolPingConnectionsNotUsedFor;
    }

    public int getExpectedConnectionTypeCode() {
        return expectedConnectionTypeCode;
    }

    public void setExpectedConnectionTypeCode(int expectedConnectionTypeCode) {
        this.expectedConnectionTypeCode = expectedConnectionTypeCode;
    }

}
