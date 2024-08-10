package com.xclhove.rpc.serializer;

import java.io.IOException;

/**
 * @author xclhove
 */
public interface Serializer {
    /**
     * 序列化
     */
    <T> byte[] serialize(T object) throws IOException;
    
    /**
     * 反序列化
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException;
}
