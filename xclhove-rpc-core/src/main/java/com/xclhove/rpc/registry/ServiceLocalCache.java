package com.xclhove.rpc.registry;

import com.xclhove.rpc.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xclhove
 */
@Slf4j
public class ServiceLocalCache {
    private final Map<String, List<ServiceMetaInfo>> localCache = new ConcurrentHashMap<>();
    
    /**
     * 写入缓存
     */
    public void write(String service, List<ServiceMetaInfo> newCache) {
        localCache.put(service, newCache);
    }
    
    /**
     * 读取缓存
     */
    public List<ServiceMetaInfo> read(String service) {
        return localCache.get(service);
    }
    
    /**
     * 清除缓存
     */
    public List<ServiceMetaInfo> remove(String service) {
        return localCache.remove(service);
    }
}
