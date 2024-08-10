package com.xclhove.rpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.alibaba.fastjson2.JSON;
import com.xclhove.rpc.config.RegistryConfig;
import com.xclhove.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 使用 etcd 实现注册中心
 * @author xclhove
 */
@Slf4j
public class EtcdRegistry implements Registry {
    private String rootPath;
    private Client client;
    private KV kvClient;
    /**
     * 已注册的服务的key，用于续期
     */
    private final Set<String> registeredServiceKey = new ConcurrentHashSet<>();
    /**
     * 是否已经启动心跳检测
     */
    private boolean enabledHeartBeat = false;
    /**
     * 服务发现缓存
     */
    private final ServiceLocalCache serviceLocalCache = new ServiceLocalCache();
    /**
     * 监听的服务的key，
     */
    private final Set<String> watchServiceKey = new ConcurrentHashSet<>();
    
    @Override
    public void init(RegistryConfig registryConfig) {
        rootPath = "/" + registryConfig.getRootPath();
        
        String address = registryConfig.getAddress();
        if (!address.startsWith("http")) {
            address = "http://" + address;
        }
        client = Client.builder()
                .endpoints(address)
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
    }
    
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建 leaseClient
        Lease leaseClient = client.getLeaseClient();
        // 创建一个 30 秒的租约
        long leaseId = leaseClient.grant(30).get().getID();
        
        // 设置要存储的键值对
        String serviceRegistryKey = getServiceRegistryKey(serviceMetaInfo);
        ByteSequence key = ByteSequence.from(serviceRegistryKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSON.toJSONString(serviceMetaInfo), StandardCharsets.UTF_8);
        
        // 将键值对关联起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();
        
        // 添加已注册的服务到本地缓存
        registeredServiceKey.add(serviceRegistryKey);
    }
    
    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        kvClient.delete(ByteSequence.from(getServiceRegistryKey(serviceMetaInfo), StandardCharsets.UTF_8));
        registeredServiceKey.remove(getServiceRegistryKey(serviceMetaInfo));
    }
    
    @Override
    public List<ServiceMetaInfo> serviceDiscover(ServiceMetaInfo serviceMetaInfo) {
        final String discoverPrefix = getDiscoverPrefix(serviceMetaInfo);
        
        // 读取本地缓存
        List<ServiceMetaInfo> localCache = serviceLocalCache.read(discoverPrefix);
        if (CollUtil.isNotEmpty(localCache)) {
            return localCache;
        }
        
        // 通过前缀查询
        try {
            List<ServiceMetaInfo> discoverResult = kvClient
                    .get(
                            ByteSequence.from(discoverPrefix, StandardCharsets.UTF_8),
                            GetOption.builder().isPrefix(true).build()
                    )
                    .get()
                    .getKvs()
                    .stream()
                    .map(keyValue -> {
                        String jsonStr = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo value = JSON.parseObject(jsonStr, ServiceMetaInfo.class);
                        // 监听该服务变化
                        watch(value);
                        return value;
                    })
                    .collect(Collectors.toList());
            // 写入本地缓存
            serviceLocalCache.write(discoverPrefix, discoverResult);
            
            return discoverResult;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }
    
    @Override
    public void destroy() {
        log.info("当前节点下线");
        registeredServiceKey.forEach(serviceRegistryKey -> {
            try {
                kvClient.delete(
                        ByteSequence.from(serviceRegistryKey, StandardCharsets.UTF_8)
                ).get();
                log.info("下线服务：{}", serviceRegistryKey);
            } catch (Exception e) {
                log.error("下线服务：{} 失败", serviceRegistryKey, e);
            }
        });
        
        if (client != null) {
            client.close();
        }
        if (kvClient != null) {
            kvClient.close();
        }
    }
    
    @Override
    public void enableHeartBeat() {
        if (enabledHeartBeat) {
            return;
        }
        enabledHeartBeat = true;
        log.info("EtcdRegistry 开启心跳检测");
        // 每 10 秒执行一次
        CronUtil.schedule("*/10 * * * * *", (Task) () -> {
            registeredServiceKey.forEach(registryKey -> {
                try {
                    List<KeyValue> keyValues = kvClient.get(
                            ByteSequence.from(registryKey, StandardCharsets.UTF_8)
                    ).get().getKvs();
                    
                    // 该节点已经过期
                    if (CollUtil.isEmpty(keyValues)) {
                        return;
                    }
                    
                    // 续签
                    KeyValue keyValue = keyValues.get(0);
                    String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                    ServiceMetaInfo serviceMetaInfo = JSON.parseObject(value, ServiceMetaInfo.class);
                    register(serviceMetaInfo);
                } catch (Exception e) {
                    log.error("{}：续签失败", registryKey);
                    throw new RuntimeException(e);
                }
            });
        });
        // 支持秒级定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }
    
    @Override
    public void watch(ServiceMetaInfo watchServiceMetaInfo) {
        String watchKey = getServiceRegistryKey(watchServiceMetaInfo);
        Watch watchClient = client.getWatchClient();
        boolean isNewWatch = watchServiceKey.add(watchKey);
        if (!isNewWatch) {
            return;
        }
        watchClient.watch(
                ByteSequence.from(watchKey, StandardCharsets.UTF_8),
                (watchResponse) -> watchResponse.getEvents().forEach(event -> {
                    if (event.getEventType() == WatchEvent.EventType.DELETE) {
                        String key = event.getKeyValue().getKey().toString(StandardCharsets.UTF_8);
                        log.info("服务 {} 下线", key);
                        String serviceDiscoverPrefix = key.substring(0, key.lastIndexOf("/") + 1);
                        // 清除本地缓存
                        serviceLocalCache.remove(serviceDiscoverPrefix);
                        watchServiceKey.remove(key);
                    }
                })
        );
    }
    
    private String getDiscoverPrefix(ServiceMetaInfo serviceMetaInfo) {
        return String.format("%s/%s/", rootPath, serviceMetaInfo.getServiceKey());
    }
    
    private String getServiceRegistryKey(ServiceMetaInfo serviceMetaInfo) {
        return String.join(
                "/",
                rootPath,
                serviceMetaInfo.getServiceKey(),
                serviceMetaInfo.getNodeKey()
        );
    }
}
