package com.xclhove.rpc.fault.retry;

import com.xclhove.rpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 不重试
 * @author xclhove
 */
public class NoRetryStrategy implements RetryStrategy {
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
