package com.code.datasource.pool;

import java.util.ArrayList;
import java.util.List;

/**
 * 池状态
 *
 * @author HeXin
 * @date 2024/01/27
 */
public class PoolState {

    /**
     * 数据源
     */
    protected PoolDataSource dataSource;

    /**
     * 空闲连接
     */
    protected final List<PoolConnection> idleConnections = new ArrayList<>();

    /**
     * 活动连接
     */
    protected final List<PoolConnection> activeConnections = new ArrayList<>();

    /**
     * 请求计数
     */
    protected long requestCount = 0;

    /**
     * 累计请求时间
     */
    protected long accumulatedRequestTime = 0;

    /**
     * 累计超时时间
     */
    protected long accumulatedCheckOutTime = 0;

    /**
     * 已声明逾期连接计数
     */
    protected long claimedOverdueConnectionCount = 0;

    /**
     * 累计签出时间逾期连接数
     */
    protected long accumulatedCheckOutTimeOfOverdueConnections = 0;

    /**
     * 累计等待时间
     */
    protected long accumulatedWaitTime = 0;

    /**
     * 等待连接数量
     */
    protected long hadToWaitCount = 0;

    /**
     * 错误连接数量
     */
    protected long badConnectionCount = 0;

    public PoolState(PoolDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public synchronized long getRequestCount() {
        return requestCount;
    }

    public synchronized long getAverageRequestTime() {
        return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
    }

    public synchronized long getAverageWaitTime() {
        return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;
    }

    public synchronized long getHadToWaitCount() {
        return hadToWaitCount;
    }

    public synchronized long getBadConnectionCount() {
        return badConnectionCount;
    }

    public synchronized long getClaimedOverdueConnectionCount() {
        return claimedOverdueConnectionCount;
    }

    public synchronized long getAverageOverdueCheckoutTime() {
        return claimedOverdueConnectionCount == 0 ? 0 : accumulatedCheckOutTimeOfOverdueConnections / claimedOverdueConnectionCount;
    }

    public synchronized long getAverageCheckoutTime() {
        return requestCount == 0 ? 0 : accumulatedCheckOutTime / requestCount;
    }

    public synchronized int getIdleConnectionCount() {
        return idleConnections.size();
    }

    public synchronized int getActiveConnectionCount() {
        return activeConnections.size();
    }

}
