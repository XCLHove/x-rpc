package com.xclhove.rpc.registry;

import com.xclhove.rpc.RpcApplication;
import com.xclhove.rpc.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xclhove
 */
@Slf4j
public final class RegistryFactory {
    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();
    
    public static Registry getInstance() {
        String registryImplClassName = RpcApplication.getConfig().getRegistryConfig().getRegistryImpl();
        Registry registry = SpiLoader.getInstance(Registry.class, registryImplClassName);
        return registry != null ? registry : DEFAULT_REGISTRY;
    }
}

