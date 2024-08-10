package com.xclhove.rpc.registry;

import com.xclhove.rpc.config.RegistryConfig;
import com.xclhove.rpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * @author xclhove
 */
public interface Registry {
    /**
     * 注册中心初始化
     */
    void init(RegistryConfig registryConfig);
    /**
     * 注册
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;
    /**
     * 注销
     */
    void unregister(ServiceMetaInfo serviceMetaInfo);
    /**
     * 服务发现
     */
    List<ServiceMetaInfo> serviceDiscover(ServiceMetaInfo serviceMetaInfo);
    /**
     * 销毁
     */
    void destroy();
    /**
     * 开启心跳检测
     */
    void enableHeartBeat();
    
    /**
     * 监听
     */
    void watch(ServiceMetaInfo serviceMetaInfo);
}
