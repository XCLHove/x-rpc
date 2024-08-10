package com.xclhove.common.service;

import com.xclhove.common.model.User;

/**
 * @author xclhove
 */
public interface UserService {
    User getUser(String name);
    User getUser(User user);
}
