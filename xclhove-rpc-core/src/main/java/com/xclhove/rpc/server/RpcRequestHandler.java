package com.xclhove.rpc.server;

import com.xclhove.rpc.factory.SingletonFactory;
import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.RpcResponse;
import com.xclhove.rpc.registry.LocalRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rpc请求处理器
 * @author xclhove
 */
public final class RpcRequestHandler {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    private static class MethodKey {
        private String serviceName;
        private String methodName;
        private Class<?>[] parameterTypes;
    }
    
    private RpcRequestHandler() {}
    
    private final static Map<MethodKey, Method> methodCache = new ConcurrentHashMap<>();
    
    public static RpcResponse handle(RpcRequest rpcRequest) {
        try {
            // 获取实现类
            Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
            
            // 获取方法
            Method method = getMethodFromCache(implClass, rpcRequest);
            
            // 调用方法，获取方法执行结果
            Object methodResult = method.invoke(SingletonFactory.getInstance(implClass), rpcRequest.getParameters());
            
            return new RpcResponse()
                    .setData(methodResult)
                    .setDataType(method.getReturnType());
        } catch (Exception e) {
            return new RpcResponse().setException(e);
        }
    }
    
    private static Method getMethodFromCache(Class<?> implClass, RpcRequest rpcRequest) {
        MethodKey methodKey = new MethodKey();
        methodKey.setServiceName(rpcRequest.getServiceName());
        methodKey.setMethodName(rpcRequest.getMethodName());
        methodKey.setParameterTypes(rpcRequest.getParameterTypes());
        
        return methodCache.computeIfAbsent(methodKey, (key) -> {
            try {
                return implClass.getDeclaredMethod(methodKey.getMethodName(), methodKey.getParameterTypes());
            } catch (Exception e) {
                return null;
            }
        });
    }
}
