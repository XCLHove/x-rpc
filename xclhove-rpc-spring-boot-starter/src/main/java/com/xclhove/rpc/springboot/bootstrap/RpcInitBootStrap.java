package com.xclhove.rpc.springboot.bootstrap;

import com.xclhove.rpc.RpcApplication;
import com.xclhove.rpc.config.RpcConfig;
import com.xclhove.rpc.registry.Registry;
import com.xclhove.rpc.registry.RegistryFactory;
import com.xclhove.rpc.server.RpcServer;
import com.xclhove.rpc.server.RpcServerFactory;
import com.xclhove.rpc.springboot.annotation.EnableRpc;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * 初始化 rpc 框架
 * @author xclhove
 */
public class RpcInitBootStrap implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(
            AnnotationMetadata annotationMetadata,
            BeanDefinitionRegistry beanDefinitionRegistry) {
        Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(EnableRpc.class.getName());
        if (annotationAttributes == null) {
            return;
        }
        boolean needServer = (boolean) annotationAttributes.get("needServer");
        
        RpcApplication.init();
        final RpcConfig rpcConfig = RpcApplication.getConfig();
        
        if (needServer) {
            RpcServer rpcServer = RpcServerFactory.getInstance();
            rpcServer.doStart(rpcConfig.getPort());
            
            Registry registry = RegistryFactory.getInstance();
            registry.init(rpcConfig.getRegistryConfig());
            registry.enableHeartBeat();
        }
    }
}
