package com.xclhove.provider;

import com.xclhove.common.service.UserService;
import com.xclhove.provider.impl.UserServiceImpl;
import com.xclhove.rpc.RpcApplication;
import com.xclhove.rpc.bootstrap.ProviderBootstrap;
import com.xclhove.rpc.config.RpcConfig;
import com.xclhove.rpc.model.ServiceRegisterInfo;
import com.xclhove.rpc.registry.LocalRegistry;
import com.xclhove.rpc.server.RpcServerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xclhove
 */
public class Provider {
    private static class Provider1 {
        public static void main(String[] args) {
            List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();
            serviceRegisterInfoList.add(new ServiceRegisterInfo<>(UserService.class, UserServiceImpl.class));
            
            ProviderBootstrap.init(serviceRegisterInfoList);
        }
    }
    
    public static class Provider2 {
        public static void main(String[] args) {
            List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();
            serviceRegisterInfoList.add(new ServiceRegisterInfo<>(UserService.class, UserServiceImpl.class));
            
            ProviderBootstrap.init(serviceRegisterInfoList);
        }
    }
}
