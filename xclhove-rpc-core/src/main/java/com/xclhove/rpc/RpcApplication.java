package com.xclhove.rpc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.xclhove.rpc.config.RegistryConfig;
import com.xclhove.rpc.config.RpcConfig;
import com.xclhove.rpc.registry.Registry;
import com.xclhove.rpc.registry.RegistryFactory;
import com.xclhove.rpc.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

import static com.xclhove.rpc.constant.RpcConstant.RPC_CONFIG_PREFIX;

/**
 * @author xclhove
 */
@Slf4j
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;
    
    public static void init(RpcConfig newConfig) {
        rpcConfig = newConfig;
        log.info("初始化成功，配置信息: {}", JSON.toJSONString(
                rpcConfig,
                JSONWriter.Feature.PrettyFormat,
                JSONWriter.Feature.WriteMapNullValue
        ));
        
        initRegistry();
    }
    
    public static void init() {
        RpcConfig rpcConfig;
        try {
            rpcConfig = ConfigUtil.loadConfig(RpcConfig.class, RPC_CONFIG_PREFIX);
        } catch (Exception e) {
            log.error("加载配置出错: {}, 使用默认配置", e.getMessage());
            rpcConfig = new RpcConfig();
        }
        init(rpcConfig);
    }
    
    public static RpcConfig getConfig() {
        if (rpcConfig != null) {
            return rpcConfig;
        }
        synchronized (RpcApplication.class) {
            if (rpcConfig == null) {
                init();
            }
        }
        return rpcConfig;
    }
    
    /**
     * 初始化注册中心
     */
    private static void initRegistry() {
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance();
        registry.init(registryConfig);
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }
}
