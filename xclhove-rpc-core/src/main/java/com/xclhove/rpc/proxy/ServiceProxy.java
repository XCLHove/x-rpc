package com.xclhove.rpc.proxy;

import cn.hutool.core.util.StrUtil;
import com.xclhove.rpc.RpcApplication;
import com.xclhove.rpc.fault.retry.RetryStrategy;
import com.xclhove.rpc.fault.retry.RetryStrategyFactory;
import com.xclhove.rpc.fault.tolerant.TolerantStrategy;
import com.xclhove.rpc.fault.tolerant.TolerantStrategyFactory;
import com.xclhove.rpc.loadbalancer.LoadBalancer;
import com.xclhove.rpc.loadbalancer.LoadBalancerFactory;
import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.RpcResponse;
import com.xclhove.rpc.model.ServiceMetaInfo;
import com.xclhove.rpc.registry.Registry;
import com.xclhove.rpc.registry.RegistryFactory;
import com.xclhove.rpc.server.RpcServer;
import com.xclhove.rpc.server.RpcServerFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author xclhove
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] parameters) {
        // 构造rpc请求
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName(method.getDeclaringClass().getName());
        rpcRequest.setParameters(parameters);
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setVersion(RpcApplication.getConfig().getVersion());
        
        // 服务发现
        Registry registry = RegistryFactory.getInstance();
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscover(new ServiceMetaInfo()
                .setName(rpcRequest.getServiceName())
                .setVersion(rpcRequest.getVersion())
        );
        
        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance();
        ServiceMetaInfo serviceMetaInfo = loadBalancer.select(rpcRequest, serviceMetaInfoList);
        if (serviceMetaInfo == null) {
            throw new RuntimeException(rpcRequest.getServiceName() + " 无服务可用");
        }
        
        // 发送请求
        RpcServer rpcServer = RpcServerFactory.getInstance();
        RpcResponse rpcResponse;
        try {
            // 重试策略
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance();
            rpcResponse = retryStrategy.doRetry(() -> {
                RpcResponse retryRpcResponse = rpcServer.sendRpcRequest(serviceMetaInfo, rpcRequest);
                Exception exception = retryRpcResponse.getException();
                if (exception != null) {
                    String message = exception.getMessage();
                    if (StrUtil.isNotBlank(message)) {
                        log.error(exception.getMessage());
                    }
                    throw exception;
                }
                return retryRpcResponse;
            });
        } catch (Exception e) {
            // 容错
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance();
            rpcResponse = tolerantStrategy.doTolerant(rpcRequest, e);
        }
        
        // 返回结果
        return rpcResponse.getData();
    }
}
