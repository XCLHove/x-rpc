package com.xclhove.rpc.loadbalancer;

import cn.hutool.core.collection.CollUtil;
import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡
 * @author xclhove
 */
public class RandomLoadBalancer implements LoadBalancer {
    private final static Random RANDOM = new Random();
    
    @Override
    public ServiceMetaInfo select(RpcRequest rpcRequest, List<ServiceMetaInfo> list) {
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        
        int size = list.size();
        if (size == 1) {
            return list.get(0);
        }
        
        return list.get(RANDOM.nextInt(size));
    }
}
