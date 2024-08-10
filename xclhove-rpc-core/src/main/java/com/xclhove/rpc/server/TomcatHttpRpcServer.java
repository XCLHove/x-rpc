package com.xclhove.rpc.server;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.xclhove.rpc.model.RpcRequest;
import com.xclhove.rpc.model.RpcResponse;
import com.xclhove.rpc.model.ServiceMetaInfo;
import com.xclhove.rpc.serializer.Serializer;
import com.xclhove.rpc.serializer.SerializerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * 基于 HTTP 协议，使用 Tomcat 实现
 * @author xclhove
 */
@Slf4j
class TomcatHttpRpcServer implements RpcServer {
    private final static String SERIALIZER_HEADER_KEY = "serializer";
    
    @Override
    public void doStart(int port) {
        Tomcat tomcat = new Tomcat();
        
        Server server = tomcat.getServer();
        
        Connector connector = new Connector();
        connector.setPort(port);
        
        String contextPath = "";
        Context context = new StandardContext();
        context.setPath(contextPath);
        context.addLifecycleListener(new Tomcat.FixContextListener());
        
        String hostname = "localhost";
        Host host = new StandardHost();
        host.setName(hostname);
        host.addChild(context);
        
        Engine engine = new StandardEngine();
        engine.setDefaultHost(hostname);
        engine.addChild(host);
        
        Service service = server.findService("Tomcat");
        service.setContainer(engine);
        service.addConnector(connector);
        
        // 处理请求
        final String dispatcherServletName = "dispatcher";
        tomcat.addServlet(contextPath, dispatcherServletName, new DispatcherHttpServlet());
        context.addServletMappingDecoded("/*", dispatcherServletName);
        
        try {
            tomcat.start();
            log.info("TomcatHttpRpcServer started on port {}", port);
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
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
    
    private static class DispatcherHttpServlet extends HttpServlet {
        @Override
        public void service(HttpServletRequest request, HttpServletResponse response) {
            try {
                // 获取请求参数
                byte[] requestBody = new byte[request.getContentLength()];
                request.getInputStream().read(requestBody);
                
                Enumeration<String> headerNames = request.getHeaderNames();
                
                // 获取序列化器
                String serializerImplClassName = request.getHeader(SERIALIZER_HEADER_KEY);
                Serializer serializer = SerializerFactory.getInstance(serializerImplClassName);
                
                // 反序列化请求参数
                RpcRequest rpcRequest = serializer.deserialize(requestBody, RpcRequest.class);
                // 处理请求，得到响应结果
                RpcResponse rpcResponse = RpcRequestHandler.handle(rpcRequest);
                // 序列化响应结果
                byte[] responseBody = serializer.serialize(rpcResponse);
                response.setHeader(SERIALIZER_HEADER_KEY, serializer.getClass().getName());
                // 返回
                IOUtils.write(responseBody, response.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
