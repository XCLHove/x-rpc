package com.xclhove.rpc.loadbalancer;

import cn.hutool.core.collection.CollUtil;
import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡
 * @author xclhove
 */
public class RoundRobinLoadBalancer implements LoadBalancer {
    private final AtomicInteger index = new AtomicInteger(0);
    
    @Override
    public ServiceMetaInfo select(RpcRequest rpcRequest, List<ServiceMetaInfo> list) {
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        
        int size = list.size();
        if (size == 1) {
            return list.get(0);
        }
        
        int currentIndex = index.getAndIncrement() % size;
        return list.get(currentIndex);
    }
}
