package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.dto.req.UserSignUpReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.UserInfoRes;
import com.socialmedia.clover_network.entity.TokenItem;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

public interface AuthenticationService {
    TokenItem loginByEmail(UserLoginReq req) throws Exception;
    void signUpNewUser(UserSignUpReq req) throws MessagingException, UnsupportedEncodingException;
    ApiResponse getUserInfo();
    ApiResponse verifyAccount(String tokenId);
}
