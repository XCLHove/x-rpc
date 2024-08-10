package com.xclhove.provider.springboot.impl;

import com.xclhove.common.model.User;
import com.xclhove.common.service.UserService;
import com.xclhove.rpc.springboot.annotation.RpcService;

/**
 * @author xclhove
 */
@RpcService(interfaceClass = UserService.class)
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(String name) {
        return new User().setName(name);
    }
    
    @Override
    public User getUser(User user) {
        return user.setId(Integer.MAX_VALUE);
    }
}
