package com.xclhove.rpc.bootstrap;

import com.xclhove.rpc.RpcApplication;
import com.xclhove.rpc.config.RpcConfig;
import com.xclhove.rpc.model.ServiceMetaInfo;
import com.xclhove.rpc.model.ServiceRegisterInfo;
import com.xclhove.rpc.registry.LocalRegistry;
import com.xclhove.rpc.registry.Registry;
import com.xclhove.rpc.registry.RegistryFactory;
import com.xclhove.rpc.server.RpcServer;
import com.xclhove.rpc.server.RpcServerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author xclhove
 */
@Slf4j
public class ProviderBootstrap {
    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {
        RpcApplication.init();
        RpcConfig rpcConfig = RpcApplication.getConfig();
        
        Registry registry = RegistryFactory.getInstance();
        registry.enableHeartBeat();
        
        // 注册服务
        serviceRegisterInfoList.forEach(serviceRegisterInfo -> {
            // 本地注册
            LocalRegistry.register(serviceRegisterInfo);
            
            // 在注册中心注册
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setPort(rpcConfig.getPort());
            serviceMetaInfo.setHost(rpcConfig.getHost());
            serviceMetaInfo.setName(serviceRegisterInfo.getServiceClass().getName());
            serviceMetaInfo.setVersion(rpcConfig.getVersion());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                log.error("注册服务 {} 失败", serviceMetaInfo.getName());
                throw new RuntimeException(e);
            }
        });
        
        // 启动 rpc 服务器
        RpcServer rpcServer = RpcServerFactory.getInstance();
        rpcServer.doStart(rpcConfig.getPort());
    }
}
