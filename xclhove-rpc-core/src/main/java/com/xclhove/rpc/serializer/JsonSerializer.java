package com.xclhove.rpc.serializer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.RpcResponse;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author xclhove
 */
public class JsonSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return JSON.toJSONBytes(object);
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        if (RpcResponse.class == clazz) {
            return (T) deserializeRpcResponse(bytes);
        }
        if (RpcRequest.class == clazz) {
            return (T) deserializeRpcRequest(bytes);
        }
        return JSON.parseObject(bytes, clazz, JSONReader.Feature.SupportClassForName);
    }
    
    private RpcRequest deserializeRpcRequest(byte[] bytes) throws IOException {
        RpcRequest rpcRequest = JSON.parseObject(bytes, RpcRequest.class, JSONReader.Feature.SupportClassForName);
        Object[] parameters = rpcRequest.getParameters();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = deserialize(serialize(parameters[i]), parameterTypes[i]);
        }
        rpcRequest.setParameters(parameters);
        return rpcRequest;
    }
    
    private RpcResponse deserializeRpcResponse(byte[] bytes) throws IOException {
        RpcResponse rpcResponse = JSON.parseObject(bytes, RpcResponse.class, JSONReader.Feature.SupportClassForName);
        rpcResponse.setData(deserialize(serialize(rpcResponse.getData()), rpcResponse.getDataType()));
        return rpcResponse;
    }
}
