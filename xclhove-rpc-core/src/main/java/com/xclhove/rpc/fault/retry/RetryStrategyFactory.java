package com.xclhove.rpc.fault.retry;

import com.xclhove.rpc.RpcApplication;
import com.xclhove.rpc.spi.SpiLoader;

/**
 * @author xclhove
 */
public class RetryStrategyFactory {
    private static final RetryStrategy DEFAULT = new NoRetryStrategy();
    
    public static RetryStrategy getInstance(String implClassName) {
        RetryStrategy instance = SpiLoader.getInstance(RetryStrategy.class, implClassName);
        return instance == null ? DEFAULT : instance;
    }
    
    public static RetryStrategy getInstance() {
        return getInstance(RpcApplication.getConfig().getRetryStrategyImpl());
    }
}
