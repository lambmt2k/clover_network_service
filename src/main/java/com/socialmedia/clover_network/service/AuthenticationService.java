package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.dto.req.UserSignUpReq;
import com.socialmedia.clover_network.entity.TokenItem;

public interface AuthenticationService {
    TokenItem loginByEmail(UserLoginReq req) throws Exception;

    void signUpNewUser(UserSignUpReq req);
}
