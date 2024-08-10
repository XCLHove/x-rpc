package com.xclhove.rpc.serializer;

import com.xclhove.rpc.RpcApplication;
import com.xclhove.rpc.spi.SpiLoader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xclhove
 */
@Slf4j
public final class SerializerFactory {
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();
    
    public static Serializer getInstance() {
        return getInstance(RpcApplication.getConfig().getSerializerImpl());
    }
    
    public static Serializer getInstance(String serializerImplClassName) {
        Serializer serializer = SpiLoader.getInstance(Serializer.class, serializerImplClassName);
        return serializer == null ? DEFAULT_SERIALIZER : serializer;
    }
}
