package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.dto.res.UserLoginRes;
import com.socialmedia.clover_network.util.SystemException;

public interface UserService {
    UserLoginRes login(UserLoginReq req) throws SystemException;
}
