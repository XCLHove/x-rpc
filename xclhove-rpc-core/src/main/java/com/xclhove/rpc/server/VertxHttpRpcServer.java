package com.xclhove.rpc.server;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.RpcResponse;
import com.xclhove.rpc.model.ServiceMetaInfo;
import com.xclhove.rpc.serializer.Serializer;
import com.xclhove.rpc.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 基于 HTTP 协议，使用 vertx 实现
 * @author xclhove
 */
@Slf4j
public class VertxHttpRpcServer implements RpcServer {
    private final static String SERIALIZER_HEADER_KEY = "serializer";
    private final Serializer serializer = SerializerFactory.getInstance();
    
    @Override
    public void doStart(int port) {
        // 创建vertx
        Vertx vertx = Vertx.vertx();
        // 创建 httpServer
        HttpServer httpServer = vertx.createHttpServer();
        // 处理请求
        httpServer.requestHandler(new RequestHandler());
        // 启动httpServer
        httpServer.listen(port, result -> {
            if (result.failed()) {
                log.error("Failed to start VertxHttpRpcServer: ", result.cause());
                return;
            }
            log.info("VertxHttpRpcServer started on port {}", port);
        });
    }
    
    @Override
    public RpcResponse sendRpcRequest(ServiceMetaInfo serviceMetaInfo, RpcRequest rpcRequest) {
        try {
            // 序列化请求参数
            Serializer requestSerializer = SerializerFactory.getInstance();
            byte[] requestBody = requestSerializer.serialize(rpcRequest);
            
            // 发送请求
            byte[] responseBody;
            String responseSerializerImplClassName;
            try (HttpResponse httpResponse = HttpRequest
                    .post("http://" + serviceMetaInfo.getNodeKey())
                    .header(SERIALIZER_HEADER_KEY, requestSerializer.getClass().getName())
                    .body(requestBody)
                    .execute()
            ) {
                responseBody = httpResponse.bodyBytes();
                responseSerializerImplClassName = httpResponse.header(SERIALIZER_HEADER_KEY);
            }
            
            // 反序列化响应结果
            Serializer responseSerializer = SerializerFactory.getInstance(responseSerializerImplClassName);
            return responseSerializer.deserialize(responseBody, RpcResponse.class);
        } catch (Exception e) {
            return new RpcResponse().setException(e);
        }
    }
    
    private static class RequestHandler implements Handler<HttpServerRequest> {
        @Override
        public void handle(HttpServerRequest request) {
            final HttpServerResponse response = request.response();
            request.bodyHandler(bodyBuffer -> {
                // 序列化器
                Serializer serializer = SerializerFactory.getInstance(request.getHeader(SERIALIZER_HEADER_KEY));
                
                RpcResponse rpcResponse;
                if (bodyBuffer.length() == 0) {
                    rpcResponse = new RpcResponse();
                    rpcResponse.setMessage("need parameter");
                    try {
                        response.putHeader(SERIALIZER_HEADER_KEY, serializer.getClass().getName());
                        response.end(Buffer.buffer(serializer.serialize(rpcResponse)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
                
                try {
                    // 获取请求参数
                    byte[] requestBody = bodyBuffer.getBytes();
                    // 反序列化请求参数
                    RpcRequest rpcRequest = serializer.deserialize(requestBody, RpcRequest.class);
                    // 处理请求，得到响应结果
                    rpcResponse = RpcRequestHandler.handle(rpcRequest);
                    // 序列化响应结果
                    byte[] responseBody = serializer.serialize(rpcResponse);
                    response.putHeader(SERIALIZER_HEADER_KEY, serializer.getClass().getName());
                    // 返回
                    response.end(Buffer.buffer(responseBody));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
