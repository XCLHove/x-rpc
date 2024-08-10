package com.xclhove.rpc.springboot.annotation;

import com.xclhove.rpc.constant.RpcConstant;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注册服务
 * @author xclhove
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {
    /**
     * 服务接口
     */
    Class<?> interfaceClass() default void.class;
    /**
     * 版本
     */
    String version() default RpcConstant.SERVICE_DEFAULT_VERSION;
}
