package com.socialmedia.clover_network.controller;

import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.dto.req.UserSignUpReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.entity.TokenItem;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.service.AuthenticationService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

@RestController
@RequestMapping(path = "/api/authenticate")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login-by-email")
    public ResponseEntity<ApiResponse> loginByEmail(HttpServletRequest request, @RequestBody UserLoginReq req) throws Exception {
        ApiResponse res = authenticationService.loginByEmail(request, req);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/get-all-user-info/{userId}")
    public ResponseEntity<ApiResponse> getAllUserInfo(@PathVariable String userId) throws Exception {
        //api test
        ApiResponse res = authenticationService.getAllUserInfo(userId);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("/signup-by-email")
    public ResponseEntity<ApiResponse> signUpByEmail(HttpServletRequest request, @RequestBody UserSignUpReq req) throws Exception {
        ApiResponse res = authenticationService.signUpNewUser(request, req);
        //validate req
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

}
