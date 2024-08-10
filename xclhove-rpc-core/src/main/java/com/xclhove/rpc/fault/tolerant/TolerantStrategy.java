package com.xclhove.rpc.fault.tolerant;

import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 容错策略
 * @author xclhove
 */
public interface TolerantStrategy {
    /**
     * 容错
     */
    RpcResponse doTolerant(RpcRequest rpcRequest, Exception exception);
}
