package com.xclhove.rpc.fault.retry;

import com.xclhove.rpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 重试策略
 * @author xclhove
 */
public interface RetryStrategy {
    /**
     * 重试
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
