package com.xclhove.rpc.loadbalancer;

import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * @author xclhove
 */
public interface LoadBalancer {
    ServiceMetaInfo select(RpcRequest rpcRequest, List<ServiceMetaInfo> serviceMetaInfoList);
}
