# X-RPC

## 模块说明

* xclhove-rpc-core：核心模块，提供 RPC 框架的功能。
* provider： 服务端模块示例代码，用于提供 RPC 服务。
* consumer： 客户端模块示例代码，用于消费服务端提供的 RPC 服务。
* xclhove-rpc-spring-boot-starter：注解驱动的 RPC 框架，可在 Spring Boot 项目中快速使用。
* provider-springboot：服务端模块示例代码，用于提供 RPC 服务（Spring Boot 框架）。
* consumer-springboot：客户端模块示例代码，用于消费服务端提供的 RPC 服务（Spring Boot 框架）。

## RPC 框架模块说明

### RPC 服务端

自带实现：

* `com.xclhove.rpc.server.TomcatHttpRpcServer`：使用 Tomcat + HTTP 协议。
* `com.xclhove.rpc.server.VertxHttpRpcServer`：使用 Vert.x + HTTP 协议。
* `com.xclhove.rpc.server.VertxTcpRpcServer`：使用 Vert.x + 自定义协议。

配置`recources/rpc-application.properties`：

```properties
rpc.port=8080
rpc.rpcServerImpl=com.xclhove.rpc.server.VertxTcpRpcServer
```

自定义实现：

1. 需要实现`com.xclhove.rpc.server.RpcServer`接口。
2. 文件`recources/META-INF/rpc/custom/com.xclhove.rpc.server.RpcServer`（文件不存在则需要自行创建）中添加自定义实现类的全类名。

---

### 序列化

自带实现：

* `com.xclhove.rpc.serializer.JdkSerializer`：基于 JDK 序列化。
* `com.xclhove.rpc.serializer.HessianSerializer`：基于 Hessian 序列化。
* `com.xclhove.rpc.serializer.JsonSerializer`：基于 JSON 序列化。

配置`recources/rpc-application.properties`：

```properties
rpc.serializerImpl=com.xclhove.rpc.serializer.JdkSerializer
```

自定义实现：

1. 需要实现`com.xclhove.rpc.serializer.Serializer`接口。
2. 文件`recources/META-INF/rpc/custom/com.xclhove.rpc.serializer.Serializer`（文件不存在则需要自行创建）中添加自定义实现类的全类名。

---

## 负载均衡

自带实现：

* `com.xclhove.rpc.loadbalancer.RoundRobinLoadBalancer`：轮询负载均衡。
* `com.xclhove.rpc.loadbalancer.RandomLoadBalancer`：随机负载均衡。
* `com.xclhove.rpc.loadbalancer.ConsistentHashLoadBalancer`：一致性哈希负载均衡。

配置`recources/rpc-application.properties`：

```properties
rpc.loadBalancerImpl=com.xclhove.rpc.loadbalancer.RoundRobinLoadBalancer
```

自定义实现：

1. 需要实现`com.xclhove.rpc.loadbalancer.LoadBalancer`接口。
2. 文件`recources/META-INF/rpc/custom/com.xclhove.rpc.loadbalancer.LoadBalancer`（文件不存在则需要自行创建）中添加自定义实现类的全类名。

---

## 重试策略

自带实现：

* `com.xclhove.rpc.fault.retry.NoRetryStrategy`：不重试。
* `com.xclhove.rpc.fault.retry.FixedIntervalRetryStrategy`：固定时间间隔重试。

配置`recources/rpc-application.properties`：

```properties
rpc.retryStrategyImpl=com.xclhove.rpc.fault.retry.FixedIntervalRetryStrategy
```

自定义实现：

1. 需要实现`com.xclhove.rpc.fault.retry.RetryStrategy`接口。
2. 文件`recources/META-INF/rpc/custom/com.xclhove.rpc.fault.retry.RetryStrategy`（文件不存在则需要自行创建）中添加自定义实现类的全类名。

---

## 容错策略

自带实现：

* `com.xclhove.rpc.fault.tolerant.FailFastTolerantStrategy`：快速失败容错策略。
* `com.xclhove.rpc.fault.tolerant.FailSafeTolerantStrategy`：静默处理异常容错策略。

配置`recources/rpc-application.properties`：

```properties
rpc.tolerantStrategyImpl=com.xclhove.rpc.fault.tolerant.FailSafeTolerantStrategy
```

自定义实现：

1. 需要实现`com.xclhove.rpc.fault.tolerant.TolerantStrategy`接口。
2. 文件`recources/META-INF/rpc/custom/com.xclhove.rpc.fault.tolerant.TolerantStrategy`（文件不存在则需要自行创建）中添加自定义实现类的全类名。

---

## 注册中心

自带实现：

* `com.xclhove.rpc.registry.EtcdRegistry`：基于 Etcd 注册中心。
* `com.xclhove.rpc.registry.ZookeeperRegistry`：基于 Zookeeper 注册中心。

配置`recources/rpc-application.properties`：

```properties
# 注册中心实现类
rpc.registryConfig.registryImpl=com.xclhove.rpc.registry.EtcdRegistry
# 注册中心地址
rpc.registryConfig.address=localhost:2379
# 注册中心根路径
rpc.registryConfig.rootPath=rpc
# 注册中心用户名
rpc.registryConfig.username=admin
# 注册中心密码
rpc.registryConfig.password=123456
# 超时时间
rpc.registryConfig.timeout=5000
```

自定义实现：

1. 需要实现`com.xclhove.rpc.registry.Registry`接口。
2. 文件`recources/META-INF/rpc/custom/com.xclhove.rpc.registry.Registry`（文件不存在则需要自行创建）中添加自定义实现类的全类名。

---

## 其他配置


### 服务端

```properties
# rpc 服务端地址
rpc.host=localhost
# 版本
rpc.version=1.0.0
```

### 客户端

```properties
# 版本
rpc.version=1.0.0
# 是否开启模拟远程调用
rpc.mock=false
```

## Spring Boot Starter 注解

* `com.xclhove.rpc.springboot.annotation.EnableRpc`：启用 RPC 框架，属性：
    * `needServer`：是否需要启动 RPC 服务端，默认 `true`。
* `com.xclhove.rpc.springboot.annotation.RpcService`：注册 RPC 服务（服务端），属性：
    * `interfaceClass`：要注册的服务接口类，默认为该类实现的第一个接口。
    * `version`：服务版本，默认 `1.0.0`。
* `com.xclhove.rpc.springboot.annotation.RpcReference`：为字段注入 RPC 服务对象（客户端），属性：
    * `interfaceClass`：要注入的服务接口类，默认为该字段类型。
