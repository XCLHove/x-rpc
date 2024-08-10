package com.xclhove.rpc.springboot.bootstrap;

import com.xclhove.rpc.proxy.ServiceProxyFactory;
import com.xclhove.rpc.springboot.annotation.RpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @author xclhove
 */
@Slf4j
public class RpcConsumerBootStrap implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        Arrays.stream(fields).forEach(field -> {
            RpcReference rpcReferenceAnnotation = field.getAnnotation(RpcReference.class);
            if (rpcReferenceAnnotation == null) {
                return;
            }
            
            Class<?> interfaceClass = rpcReferenceAnnotation.interfaceClass();
            if (interfaceClass == void.class) {
                interfaceClass = field.getType();
            }
            
            field.setAccessible(true);
            Object proxyObject = ServiceProxyFactory.getProxy(interfaceClass);
            try {
                field.set(bean, proxyObject);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(String.format("为字段 %s 注入代理对象失败",  field.getName()), e);
            } finally {
                field.setAccessible(false);
            }
        });
        
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }
}
