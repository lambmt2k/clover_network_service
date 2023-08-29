package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.entity.TokenItem;
import com.socialmedia.clover_network.service.AuthenticationService;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {


    @Override
    public TokenItem loginByEmail(UserLoginReq req) {
        return null;
    }
}
