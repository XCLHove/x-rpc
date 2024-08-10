package com.xclhove.rpc.server;

import com.xclhove.rpc.RpcApplication;
import com.xclhove.rpc.spi.SpiLoader;

/**
 * @author xclhove
 */
public final class RpcServerFactory {
    private static final RpcServer DEFAULT_SERVER = new TomcatHttpRpcServer();
    public static RpcServer getInstance() {
        RpcServer instance = SpiLoader.getInstance(RpcServer.class, RpcApplication.getConfig().getRpcServerImpl());
        return instance == null ? DEFAULT_SERVER : instance;
    }
}
