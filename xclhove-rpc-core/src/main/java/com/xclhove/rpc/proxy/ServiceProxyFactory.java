package com.xclhove.rpc.proxy;

import com.xclhove.rpc.RpcApplication;

import java.lang.reflect.Proxy;

/**
 * @author xclhove
 */
public class ServiceProxyFactory {
    public static <T> T getProxy(final Class<T> interfaceClass) {
        if (RpcApplication.getConfig().isMock()) {
            return getMockProxy(interfaceClass);
        }
        
        Object proxyInstance = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ServiceProxy()
        );
        return (T) proxyInstance;
    }
    
    private static <T> T getMockProxy(final Class<T> interfaceClass) {
        Object proxyInstance = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new MockServiceProxy()
        );
        return (T) proxyInstance;
    }
}
