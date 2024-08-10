package com.xclhove.rpc.model;

import com.xclhove.rpc.constant.RpcConstant;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author xclhove
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RpcRequest implements Serializable {
    /**
     * 服务名
     */
    private String serviceName;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数类型列表
     */
    private Class<?>[] parameterTypes;
    /**
     * 参数列表
     */
    private Object[] parameters;
    /**
     * 版本
     */
    private String version = RpcConstant.SERVICE_DEFAULT_VERSION;
}
