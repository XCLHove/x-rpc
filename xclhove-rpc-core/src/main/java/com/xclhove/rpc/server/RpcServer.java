package com.xclhove.rpc.server;

import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.RpcResponse;
import com.xclhove.rpc.model.ServiceMetaInfo;

/**
 * @author xclhove
 */
public interface RpcServer {
    /**
     * 启动服务
     */
    void doStart(int port);
    
    /**
     * 发送rpc请求
     */
    RpcResponse sendRpcRequest(ServiceMetaInfo serviceMetaInfo, RpcRequest rpcRequest);
}
