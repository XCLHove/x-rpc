package com.xclhove.rpc.registry;

import com.xclhove.rpc.model.ServiceRegisterInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xclhove
 */
@Slf4j
public class LocalRegistry {
    private static final Map<String, Class<?>> REGISTERED_SERVICE = new ConcurrentHashMap<>();
    
    public static void register(String serviceName, Class<?> implClass) {
        REGISTERED_SERVICE.put(serviceName, implClass);
    }
    
    public static <T> void register(Class<T> interfaceClass, Class<? extends T> implClass) {
        register(interfaceClass.getName(), implClass);
    }
    
    public static <T> void register(ServiceRegisterInfo<T> serviceRegisterInfo) {
        register(serviceRegisterInfo.getServiceClass(), serviceRegisterInfo.getImplClass());
    }
    
    public static Class<?> get(String interfaceName) {
        return REGISTERED_SERVICE.get(interfaceName);
    }
    
    public static <T> Class<? extends T> get(Class<T> interfaceClass) {
        return (Class<? extends T>) get(interfaceClass.getName());
    }
    
    public static void remove(String interfaceName) {
        REGISTERED_SERVICE.remove(interfaceName);
    }
    
    public static void remove(Class<?> interfaceClass) {
        remove(interfaceClass.getName());
    }
}
