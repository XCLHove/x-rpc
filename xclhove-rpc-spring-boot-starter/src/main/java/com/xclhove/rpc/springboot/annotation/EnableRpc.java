package com.xclhove.rpc.springboot.annotation;

import com.xclhove.rpc.springboot.bootstrap.RpcConsumerBootStrap;
import com.xclhove.rpc.springboot.bootstrap.RpcInitBootStrap;
import com.xclhove.rpc.springboot.bootstrap.RpcProviderBootStrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用 rpc
 * @author xclhove
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootStrap.class, RpcProviderBootStrap.class, RpcConsumerBootStrap.class})
public @interface EnableRpc {
    /**
     * 是否需要启动服务器
     */
    boolean needServer() default true;
}
