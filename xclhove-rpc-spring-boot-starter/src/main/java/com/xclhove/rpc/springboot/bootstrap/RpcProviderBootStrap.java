package com.xclhove.rpc.springboot.bootstrap;

import com.xclhove.rpc.RpcApplication;
import com.xclhove.rpc.config.RegistryConfig;
import com.xclhove.rpc.config.RpcConfig;
import com.xclhove.rpc.model.ServiceMetaInfo;
import com.xclhove.rpc.registry.LocalRegistry;
import com.xclhove.rpc.registry.Registry;
import com.xclhove.rpc.registry.RegistryFactory;
import com.xclhove.rpc.springboot.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Arrays;

/**
 * @author xclhove
 */
@Slf4j
public class RpcProviderBootStrap implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService rpcServiceAnnotation = beanClass.getAnnotation(RpcService.class);
        if (rpcServiceAnnotation == null) {
            return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
        }
        
        Class<?> interfaceClass = rpcServiceAnnotation.interfaceClass();
        if (interfaceClass == void.class) {
            interfaceClass = beanClass.getInterfaces()[0];
        }
        
        String serviceName = interfaceClass.getName();
        String version = rpcServiceAnnotation.version();
        
        // 本地注册
        LocalRegistry.register(serviceName, beanClass);
        
        final RpcConfig rpcConfig = RpcApplication.getConfig();
        
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setName(serviceName);
        serviceMetaInfo.setVersion(version);
        serviceMetaInfo.setHost(rpcConfig.getHost());
        serviceMetaInfo.setPort(rpcConfig.getPort());
        
        Registry registry = RegistryFactory.getInstance();
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            LocalRegistry.remove(serviceName);
            log.error("注册服务 {}:{} 失败", serviceName, version);
        }
        
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }
}
