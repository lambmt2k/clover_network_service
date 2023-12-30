package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.req.ResetPasswordDTO;
import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.dto.req.UserSignUpReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.UserInfoRes;
import com.socialmedia.clover_network.entity.TokenItem;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

public interface AuthenticationService {
    ApiResponse loginByEmail(HttpServletRequest request, UserLoginReq req) throws Exception;

    ApiResponse getAllUserInfo(String userId) throws Exception;
    ApiResponse signUpNewUser(HttpServletRequest request, UserSignUpReq req) throws Exception;
    ApiResponse verifyAccount(String tokenId);
    TokenItem getTokenItem(String tokenId);
    ApiResponse logout(String tokenId, HttpServletRequest request);
    ApiResponse generateOTP(String email);
    ApiResponse resetPassword(ResetPasswordDTO resetPasswordDTO) throws Exception;
}
