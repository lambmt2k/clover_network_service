package com.socialmedia.clover_network.controller;

import com.socialmedia.clover_network.constant.CommonRegex;
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

@RestController
@RequestMapping(path = "/api/authenticate")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login-by-email")
    public ResponseEntity<TokenItem> loginByEmail(HttpServletRequest request, @RequestBody UserLoginReq req) throws Exception {

        //validate req
        if (!req.getEmail().contains(CommonRegex.REGEX_EMAIL)) {
            return ResponseEntity.badRequest().build();
        }
        TokenItem res = authenticationService.loginByEmail(req);
        if (res.isValidTokenItem()) {
            return ResponseEntity.status(HttpStatus.OK).body(res);
        }
        return null;
    }

    @PostMapping("/signup-by-email")
    public ResponseEntity<ApiResponse> signUpByEmail(HttpServletRequest request, @RequestBody UserSignUpReq req) throws MessagingException, UnsupportedEncodingException {
        ApiResponse res = authenticationService.signUpNewUser(request, req);
        //validate req
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/get-user-info")
    public ResponseEntity<ApiResponse> getInfo(){
        ApiResponse res = authenticationService.getUserInfo();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse> verifyAccount(@RequestParam("tokenId") String tokenId){
        ApiResponse res = authenticationService.verifyAccount(tokenId);
        return ResponseEntity.ok(res);
    }
}
