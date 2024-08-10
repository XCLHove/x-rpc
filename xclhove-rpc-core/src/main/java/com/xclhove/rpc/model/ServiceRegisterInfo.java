package com.xclhove.rpc.model;

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
public class ServiceRegisterInfo<T> {
    /**
     * 服务
     */
    private Class<T> serviceClass;
    /**
     * 服务实现类
     */
    private Class<? extends T> implClass;
}
