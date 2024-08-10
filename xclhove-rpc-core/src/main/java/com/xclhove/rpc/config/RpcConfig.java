package com.xclhove.rpc.config;

import com.xclhove.rpc.fault.retry.FixedIntervalRetryStrategy;
import com.xclhove.rpc.fault.retry.NoRetryStrategy;
import com.xclhove.rpc.fault.tolerant.FailFastTolerantStrategy;
import com.xclhove.rpc.fault.tolerant.FailSafeTolerantStrategy;
import com.xclhove.rpc.loadbalancer.RandomLoadBalancer;
import com.xclhove.rpc.loadbalancer.RoundRobinLoadBalancer;
import com.xclhove.rpc.serializer.JdkSerializer;
import com.xclhove.rpc.server.VertxHttpRpcServer;
import com.xclhove.rpc.server.VertxTcpRpcServer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author xclhove
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RpcConfig {
    /**
     * 名称
     */
    private String name;
    /**
     * 版本号
     */
    private String version;
    /**
     * 主机名
     */
    private String host;
    /**
     * 端口
     */
    private int port = 8080;
    /**
     * 模拟调用
     */
    private boolean mock = false;
    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
    /**
     * 序列化器实现类名
     */
    private String serializerImpl = JdkSerializer.class.getName();
    /**
     * rpc服务器实现类名
     */
    private String rpcServerImpl = VertxTcpRpcServer.class.getName();
    /**
     * 负载均衡器实现类名
     */
    private String loadBalancerImpl = RoundRobinLoadBalancer.class.getName();
    /**
     * 重试策略实现类名
     */
    private String retryStrategyImpl = FixedIntervalRetryStrategy.class.getName();
    /**
     * 容错策略实现类名
     */
    private String tolerantStrategyImpl = FailSafeTolerantStrategy.class.getName();
}
