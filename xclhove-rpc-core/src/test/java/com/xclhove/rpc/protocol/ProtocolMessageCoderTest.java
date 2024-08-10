package com.xclhove.rpc.protocol;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.xclhove.rpc.constant.RpcConstant;
import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.protocol.enums.ProtocolMessageStatus;
import com.xclhove.rpc.protocol.enums.ProtocolMessageType;
import com.xclhove.rpc.serializer.JsonSerializer;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ProtocolMessageCoderTest {
    
    @Test
    void testEncodeAndDecode() throws IOException {
        ProtocolMessage.Header requestHeader = new ProtocolMessage.Header();
        requestHeader.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        requestHeader.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        requestHeader.setType((byte) ProtocolMessageType.REQUEST.getValue());
        requestHeader.setStatus((byte) ProtocolMessageStatus.OK.getValue());
        requestHeader.setRequestId(IdUtil.getSnowflakeNextId());
        
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("UserService");
        rpcRequest.setMethodName("getUser");
        rpcRequest.setVersion(RpcConstant.SERVICE_DEFAULT_VERSION);
        rpcRequest.setParameterTypes(new Class[]{String.class});
        rpcRequest.setParameters(new Object[]{"test-xclhove"});
        
        ProtocolMessage<RpcRequest> requestMessage = new ProtocolMessage<>();
        requestMessage.setHeader(requestHeader);
        requestMessage.setSerializerClassName(JsonSerializer.class.getName());
        requestMessage.setBody(rpcRequest);
        
        Buffer encodeBuffer = ProtocolMessageCoder.encode(requestMessage);
        
        ProtocolMessage<?> decodeMessage = ProtocolMessageCoder.decode(encodeBuffer);
        
        System.out.println(JSON.toJSONString(
                decodeMessage,
                JSONWriter.Feature.PrettyFormat,
                JSONWriter.Feature.WriteMapNullValue
        ));
        Assert.notNull(decodeMessage);
    }
}