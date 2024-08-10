package com.xclhove.rpc.config;

import com.xclhove.rpc.registry.EtcdRegistry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author xclhove
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class RegistryConfig {
    /**
     * 注册中心实现类
     */
    String registryImpl = EtcdRegistry.class.getName();
    /**
     * 注册中心地址
     */
    String address = "localhost:2379";
    /**
     * 注册中心根路径
     */
    String rootPath = "rpc";
    /**
     * 用户名
     */
    String username;
    /**
     * 密码
     */
    String password;
    /**
     * 超时时间(ms)
     */
    Long timeout = 10000L;
}
