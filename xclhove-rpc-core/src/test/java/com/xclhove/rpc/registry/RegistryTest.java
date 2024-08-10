package com.xclhove.rpc.registry;

import com.xclhove.rpc.model.ServiceMetaInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistryTest {
    private Registry registry;
    
    @BeforeEach
    void init() {
        registry = RegistryFactory.getInstance();
    }
    
    @Test
    void register() throws Exception {
        registry.register(new ServiceMetaInfo()
                .setName("TestService")
                .setHost("localhost")
                .setPort(1111)
                .setVersion("1.0.0"));
        registry.register(new ServiceMetaInfo()
                .setName("TestService")
                .setHost("localhost")
                .setPort(2222)
                .setVersion("1.0.0"));
        
        registry.register(new ServiceMetaInfo()
                .setName("TestService")
                .setHost("localhost")
                .setPort(1111)
                .setVersion("2.0.0"));
        registry.register(new ServiceMetaInfo()
                .setName("TestService")
                .setHost("localhost")
                .setPort(2222)
                .setVersion("2.0.0"));
        
        registry.register(new ServiceMetaInfo()
                .setName("Test2Service")
                .setHost("localhost")
                .setPort(1111)
                .setVersion("1.0.0"));
        registry.register(new ServiceMetaInfo()
                .setName("Test2Service")
                .setHost("localhost")
                .setPort(2222)
                .setVersion("1.0.0"));
        
        registry.register(new ServiceMetaInfo()
                .setName("TestService")
                .setHost("127.0.0.1")
                .setPort(1111)
                .setVersion("1.0.0"));
        registry.register(new ServiceMetaInfo()
                .setName("TestService")
                .setHost("127.0.0.1")
                .setPort(2222)
                .setVersion("1.0.0"));
    }
    
    @Test
    void unregister() throws Exception {
        register();
        registry.unregister(new ServiceMetaInfo()
                .setName("TestService")
                .setHost("localhost")
                .setPort(1111)
                .setVersion("1.0.0"));
        Thread.sleep(5 * 1000L);
    }
    
    @Test
    void serviceDiscover() {
        System.out.println(registry.serviceDiscover(new ServiceMetaInfo()
                .setName("TestService")
                .setHost("localhost")
                .setPort(1111)
                .setVersion("1.0.0")
        ));
    }
    
    @Test
    void destroy() {
        registry.destroy();
    }
    
    @Test
    void heartBeat() throws Exception {
        register();
        Thread.sleep(60 * 1000L);
    }
}