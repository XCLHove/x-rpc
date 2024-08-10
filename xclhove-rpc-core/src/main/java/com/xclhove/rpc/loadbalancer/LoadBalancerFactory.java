package com.xclhove.rpc.loadbalancer;

import com.xclhove.rpc.RpcApplication;
import com.xclhove.rpc.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xclhove
 */
public final class LoadBalancerFactory {
    private final static LoadBalancer DEFAULT_LOAD_BALANCER = new RandomLoadBalancer();
    
    public static LoadBalancer getInstance() {
        LoadBalancer instance = SpiLoader.getInstance(LoadBalancer.class, RpcApplication.getConfig().getLoadBalancerImpl());
        return instance == null ? DEFAULT_LOAD_BALANCER : instance;
    }
}
