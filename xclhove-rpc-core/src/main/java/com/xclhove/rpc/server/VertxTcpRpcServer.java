package com.xclhove.rpc.server;

import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.RpcResponse;
import com.xclhove.rpc.model.ServiceMetaInfo;
import com.xclhove.rpc.protocol.ProtocolMessage;
import com.xclhove.rpc.protocol.ProtocolMessageCoder;
import com.xclhove.rpc.protocol.TcpBufferHandlerWrapper;
import com.xclhove.rpc.protocol.enums.ProtocolMessageStatus;
import com.xclhove.rpc.protocol.enums.ProtocolMessageType;
import com.xclhove.rpc.serializer.Serializer;
import com.xclhove.rpc.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * 基于 TCP 实现自定义协议，使用 Vertx 实现
 * @author xclhove
 */
@Slf4j
public class VertxTcpRpcServer implements RpcServer {
    
    @Override
    public void doStart(int port) {
        Vertx vertx = Vertx.vertx();
        // 创建 TCP 服务器
        NetServer server = vertx.createNetServer();
        // 处理请求
        server.connectHandler(new RequestNetSockHandler());
        // 启动服务器
        server.listen(port, result -> {
            if (result.failed()) {
                log.error("Failed to start VertxTcpRpcServer: ", result.cause());
                return;
            }
            log.info("VertxTcpRpcServer started on port {}", port);
        });
    }
    
    @Override
    public RpcResponse sendRpcRequest(ServiceMetaInfo serviceMetaInfo, RpcRequest rpcRequest) {
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> rpcResponseFuture = new CompletableFuture<>();
        netClient.connect(
                serviceMetaInfo.getPort(),
                serviceMetaInfo.getHost(),
                result -> {
                    if (result.failed()) {
                        rpcResponseFuture.complete(new RpcResponse().setException(new Exception(result.cause())));
                        return;
                    }
                    
                    Serializer serializer = SerializerFactory.getInstance();
                    
                    // 构造请求消息头
                    ProtocolMessage.Header requestMessageHeader = new ProtocolMessage.Header();
                    requestMessageHeader.setType((byte) ProtocolMessageType.REQUEST.getValue());
                    
                    // 构造消息
                    ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>(
                            requestMessageHeader,
                            serializer.getClass().getName(),
                            rpcRequest
                    );
                    
                    NetSocket socket = result.result();
                    try {
                        // 编码
                        Buffer requestBuffer = ProtocolMessageCoder.encode(protocolMessage);
                        // 发送请求
                        socket.write(requestBuffer);
                    } catch (Exception e) {
                        log.error("ProtocolMessage 编码错误", e);
                        rpcResponseFuture.complete(new RpcResponse().setException(e));
                        return;
                    }
                    
                    // 处理响应
                    socket.handler(new TcpBufferHandlerWrapper(responseBuffer -> {
                        try {
                            ProtocolMessage<RpcResponse> responseMessage = ProtocolMessageCoder.decode(responseBuffer);
                            rpcResponseFuture.complete(responseMessage.getBody());
                        } catch (IOException e) {
                            log.error("ProtocolMessage 解码错误", e);
                            rpcResponseFuture.complete(new RpcResponse().setException(e));
                        }
                    }));
                });
        try {
            RpcResponse rpcResponse = rpcResponseFuture.get();
            netClient.close();
            return rpcResponse;
        } catch (Exception e) {
            log.error("sendRpcRequest error: ", e);
            return new RpcResponse().setException(e);
        }
    }
    
    /**
     * 处理请求
     */
    private static class RequestNetSockHandler implements Handler<NetSocket> {
        
        @Override
        public void handle(NetSocket netSocket) {
            netSocket.handler(new TcpBufferHandlerWrapper(requestBuffer -> {
                try {
                    // 解码请求
                    ProtocolMessage<RpcRequest> requestMessage = ProtocolMessageCoder.decode(requestBuffer);
                    
                    // 处理请求
                    RpcResponse rpcResponse = RpcRequestHandler.handle(requestMessage.getBody());
                    
                    // 构造响应消息头
                    ProtocolMessage.Header responseHeader = requestMessage.getHeader();
                    responseHeader.setType((byte) ProtocolMessageType.RESPONSE.getValue());
                    responseHeader.setStatus((byte) ProtocolMessageStatus.OK.getValue());
                    
                    // 构造响应消息
                    ProtocolMessage<RpcResponse> responseMessage = new ProtocolMessage<>(
                            responseHeader,
                            requestMessage.getSerializerClassName(),
                            rpcResponse
                    );
                    
                    Buffer responseBuffer = ProtocolMessageCoder.encode(responseMessage);
                    netSocket.write(responseBuffer);
                } catch (Exception e) {
                    RpcResponse rpcResponse = new RpcResponse();
                    rpcResponse.setException(e);
                    
                    Serializer serializer = SerializerFactory.getInstance();
                    
                    ProtocolMessage.Header responseHeader = new ProtocolMessage.Header();
                    responseHeader.setType((byte) ProtocolMessageType.RESPONSE.getValue());
                    responseHeader.setStatus((byte) ProtocolMessageStatus.BAD_RESPONSE.getValue());
                    
                    ProtocolMessage<RpcResponse> responseMessage = new ProtocolMessage<>(
                            responseHeader,
                            serializer.getClass().getName(),
                            rpcResponse
                    );
                    
                    Buffer responseBuffer = Buffer.buffer();
                    try {
                        responseBuffer = ProtocolMessageCoder.encode(responseMessage);
                    } catch (IOException ex) {
                        log.error("编码失败", ex);
                    }
                    
                    netSocket.write(responseBuffer);
                }
            }));
        }
    }
}
