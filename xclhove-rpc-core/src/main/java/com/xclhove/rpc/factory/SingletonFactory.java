package com.xclhove.rpc.factory;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例工厂
 * @author xclhove
 */
@Slf4j
public final class SingletonFactory {
    private final static Map<String, Object> objects = new ConcurrentHashMap<>();
    
    private SingletonFactory() {}
    
    public static <T> T getInstance(Class<T> clazz) {
        return (T) objects.computeIfAbsent(clazz.getName(), key -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            }catch (Exception e) {
                log.error("getSingletonInstance error: {}", e.getMessage());
                return null;
            }
        });
    }
}
