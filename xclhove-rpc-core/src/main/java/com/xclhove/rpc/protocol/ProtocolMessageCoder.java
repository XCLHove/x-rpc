package com.xclhove.rpc.protocol;

import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.RpcResponse;
import com.xclhove.rpc.protocol.enums.ProtocolMessageType;
import com.xclhove.rpc.serializer.Serializer;
import com.xclhove.rpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author xclhove
 */
@Slf4j
public class ProtocolMessageCoder {
    /**
     * 编码
     */
    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException {
        if (protocolMessage == null) {
            return Buffer.buffer();
        }
        
        ProtocolMessage.Header header = protocolMessage.getHeader();
        if (header == null) {
            return Buffer.buffer();
        }
        
        Serializer serializer = SerializerFactory.getInstance(protocolMessage.getSerializerClassName());
        byte[] serializerClassName = serializer.getClass().getName().getBytes();
        // 序列化得到响应体
        byte[] body = serializer.serialize(protocolMessage.getBody());
        
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());
        buffer.appendInt(serializerClassName.length);
        buffer.appendInt(body.length);
        buffer.appendBytes(serializerClassName);
        buffer.appendBytes(body);
        
        return buffer;
    }
    
    /**
     * 解码
     */
    public static <T> ProtocolMessage<T> decode(Buffer buffer) throws IOException {
        BufferReader bufferReader = new BufferReader(buffer);
        
        byte magic = bufferReader.readByte();
        if (magic != ProtocolConstant.PROTOCOL_MAGIC) {
            throw new RuntimeException("magic 非法");
        }
        
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(magic);
        header.setVersion(bufferReader.readByte());
        header.setType(bufferReader.readByte());
        header.setStatus(bufferReader.readByte());
        header.setRequestId(bufferReader.readLong());
        header.setSerializerClassNameLength(bufferReader.readInt());
        header.setBodyLength(bufferReader.readInt());
        
        // 只读取指定长度的数据，避免粘包问题
        String serializerImplClassName = bufferReader.readString(header.getSerializerClassNameLength());
        byte[] bodyBytes = bufferReader.readBytes(header.getBodyLength());
        
        // 序列化器
        Serializer serializer = SerializerFactory.getInstance(serializerImplClassName);
        
        ProtocolMessageType messageType = ProtocolMessageType.getEnumByValue(header.getType());
        if (messageType == null) {
            throw new RuntimeException("消息类型不存在");
        }
        switch (messageType) {
            case REQUEST:
                return new ProtocolMessage<T>(
                        header,
                        serializer.getClass().getName(),
                        (T) serializer.deserialize(bodyBytes, RpcRequest.class)
                );
            case RESPONSE:
                return new ProtocolMessage<>(
                        header,
                        serializer.getClass().getName(),
                        (T) serializer.deserialize(bodyBytes, RpcResponse.class)
                );
            case HEART_BEAT:
            case UNKNOWN:
            default:
                throw new RuntimeException("暂不支持该消息类型");
        }
    }
}
