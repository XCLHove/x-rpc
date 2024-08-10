package com.xclhove.rpc.fault.tolerant;

import cn.hutool.core.util.StrUtil;
import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 静默处理异常-容错策略
 * @author xclhove
 */
@Slf4j
public class FailSafeTolerantStrategy implements TolerantStrategy {
    @Override
    public RpcResponse doTolerant(RpcRequest rpcRequest, Exception exception) {
        String message = exception.getMessage();
        if (StrUtil.isNotBlank(message)) {
            log.info("静默处理异常：{}", message);
        } else {
            log.info("静默处理异常：", exception);
        }
        return new RpcResponse();
    }
}
