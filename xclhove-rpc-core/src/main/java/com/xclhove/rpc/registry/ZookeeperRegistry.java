package com.xclhove.rpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import com.xclhove.rpc.config.RegistryConfig;
import com.xclhove.rpc.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 使用 zookeeper 实现注册中心
 * @author xclhove
 */
@Slf4j
public class ZookeeperRegistry implements Registry {
    private String rootPath;
    private CuratorFramework client;
    private ServiceDiscovery<ServiceMetaInfo> serviceDiscovery;
    /**
     * 已注册的服务的key，用于续期
     */
    private final Set<String> registeredServiceKey = new ConcurrentHashSet<>();
    /**
     * 服务发现缓存，服务发现先从本地缓存读取，如果本地缓存没有，则从注册中心读取，然后放入本地缓存
     */
    private final ServiceLocalCache serviceLocalCache = new ServiceLocalCache();
    /**
     * 监听的服务的key，避免重复监听
     */
    private final Set<String> watchServiceKey = new ConcurrentHashSet<>();
    /**
     * 是否已经开启心跳检测
     */
    private boolean enabledHeartBeat = false;
    
    @Override
    public void init(RegistryConfig registryConfig) {
        rootPath = "/" + registryConfig.getRootPath();
        
        client = CuratorFrameworkFactory
                .builder()
                .connectString(registryConfig.getAddress())
                .retryPolicy(new ExponentialBackoffRetry(Math.toIntExact(registryConfig.getTimeout()), 3))
                .build();
        
        serviceDiscovery = ServiceDiscoveryBuilder
                .builder(ServiceMetaInfo.class)
                .client(client)
                .basePath(rootPath)
                .serializer(new JsonInstanceSerializer<>(ServiceMetaInfo.class))
                .build();
        
        client.start();
        try {
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 注册服务到 zookeeper 注册中心
        serviceDiscovery.registerService(buildServiceInstance(serviceMetaInfo));
        
        registeredServiceKey.add(getServiceRegistryKey(serviceMetaInfo));
    }
    
    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        // 从 zookeeper 注册中心移除
        try {
            serviceDiscovery.unregisterService(buildServiceInstance(serviceMetaInfo));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        registeredServiceKey.remove(getServiceRegistryKey(serviceMetaInfo));
    }
    
    @Override
    public List<ServiceMetaInfo> serviceDiscover(ServiceMetaInfo serviceMetaInfo) {
        final String serviceDiscoveryPrefix = getServiceDiscoveryPrefix(serviceMetaInfo);
        // 读取本地缓存
        List<ServiceMetaInfo> localCache = serviceLocalCache.read(serviceDiscoveryPrefix);
        if (CollUtil.isNotEmpty(localCache)) {
            return localCache;
        }
        
        try {
            List<ServiceMetaInfo> serviceDiscoveryResult = serviceDiscovery
                    .queryForInstances(serviceDiscoveryPrefix)
                    .stream()
                    .map(item -> {
                        ServiceMetaInfo payload = item.getPayload();
                        watch(payload);
                        return payload;
                    })
                    .collect(Collectors.toList());
            // 更新本地缓存
            if (CollUtil.isNotEmpty(serviceDiscoveryResult)) {
                serviceLocalCache.write(serviceDiscoveryPrefix, serviceDiscoveryResult);
            }
            return serviceDiscoveryResult;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void destroy() {
        log.info("当前节点下线");
        registeredServiceKey.forEach(serviceKey -> {
            try {
                client.delete().guaranteed().forPath(serviceKey);
                log.info("下线服务：{}", serviceKey);
            } catch (Exception e) {
                log.error("下线服务：{} 失败", serviceKey, e);
            }
        });
    }
    
    @Override
    public void enableHeartBeat() {
        if (enabledHeartBeat) {
            return;
        }
        enabledHeartBeat = true;
        
        log.warn("ZookeeperRegistry 无需心跳检测，建立临时节点，如果服务器故障，则临时节点直接丢失");
    }
    
    @Override
    public void watch(ServiceMetaInfo watchServiceMetaInfo) {
        String watchKey = getServiceRegistryKey(watchServiceMetaInfo);
        boolean isNewWatch = watchServiceKey.add(watchKey);
        if (!isNewWatch) {
            return;
        }
        
        CuratorCache curatorCache = CuratorCache.build(client, watchKey);
        curatorCache.start();
        curatorCache.listenable().addListener(CuratorCacheListener
                .builder()
                .forDeletes(childData -> {
                    String serviceKey = childData.getPath();
                    String discoveryKey = serviceKey.replace(rootPath, "");
                    discoveryKey = discoveryKey.substring(0, discoveryKey.lastIndexOf("/") + 1);
                    try {
                        log.info("服务 {} 下线", serviceKey);
                        serviceLocalCache.remove(discoveryKey);
                        watchServiceKey.remove(serviceKey);
                    } catch (Exception e) {
                        log.error("下线服务 {} 缓存失败", serviceKey, e);
                    }
                })
                .forChanges((oldData, newData) -> {
                    try {
                        byte[] data = oldData.getData();
                        ServiceMetaInfo serviceMetaInfo = new JsonInstanceSerializer<>(ServiceMetaInfo.class)
                                .deserialize(data)
                                .getPayload();
                        serviceLocalCache.remove(getServiceDiscoveryPrefix(serviceMetaInfo));
                    } catch (Exception e) {
                        log.error("清除本地服务 {} 缓存失败", watchKey, e);
                    }
                })
                .build()
        );
    }
    
    private ServiceInstance<ServiceMetaInfo> buildServiceInstance(ServiceMetaInfo serviceMetaInfo) {
     String nodeKey = serviceMetaInfo.getNodeKey();
     
     try {
         return ServiceInstance
                 .<ServiceMetaInfo>builder()
                 .id(nodeKey)
                 .name(serviceMetaInfo.getServiceKey())
                 .address(nodeKey)
                 .payload(serviceMetaInfo)
                 .build();
     } catch (Exception e) {
         throw new RuntimeException(e);
     }
    }
    
    private String getServiceDiscoveryPrefix(ServiceMetaInfo serviceMetaInfo) {
        return String.format("/%s/", serviceMetaInfo.getServiceKey());
    }
    
    private String getServiceRegistryKey(ServiceMetaInfo serviceMetaInfo) {
        return String.join("/", rootPath, serviceMetaInfo.getServiceKey(), serviceMetaInfo.getNodeKey());
    }
}
