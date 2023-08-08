package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.controller.dto.UserLoginReq;
import com.socialmedia.clover_network.controller.dto.UserLoginRes;
import com.socialmedia.clover_network.util.SystemException;

public interface UserService {
    UserLoginRes login(UserLoginReq req) throws SystemException;
}
