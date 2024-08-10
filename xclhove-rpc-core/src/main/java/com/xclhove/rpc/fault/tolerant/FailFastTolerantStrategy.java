package com.xclhove.rpc.fault.tolerant;

import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.RpcResponse;

/**
 * 快速失败-容错策略
 * @author xclhove
 */
public class FailFastTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(RpcRequest rpcRequest, Exception exception) {
        throw new RuntimeException("服务出错", exception);
    }
}
