package com.xclhove.rpc.loadbalancer;

import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性哈希负载均衡
 * @author xclhove
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {
    /**
     * 一致性 hash 环，存放虚拟节点
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();
    /**
     * 虚拟节点数量
     */
    private static final int VIRTUAL_NODES_COUNT = 100;
    @Override
    public ServiceMetaInfo select(RpcRequest rpcRequest, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }
        
        // 构建虚拟节点环
        serviceMetaInfoList.forEach(serviceMetaInfo -> {
            for (int i = 0; i < VIRTUAL_NODES_COUNT; i++) {
                int hash = getHash(String.format("%s#%d", serviceMetaInfo.getNodeKey(), i));
                virtualNodes.put(hash, serviceMetaInfo);
            }
        });
        
        int hash = getHash(rpcRequest);
        
        // 选择最接近且大于等于调用请求 hash 的节点
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);
        if (entry == null) {
            return virtualNodes.firstEntry().getValue();
        }
        return entry.getValue();
    }
    
    private static int getHash(Object key) {
        return key.hashCode();
    }
}
