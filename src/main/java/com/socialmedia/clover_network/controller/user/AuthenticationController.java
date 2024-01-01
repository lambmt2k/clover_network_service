package com.socialmedia.clover_network.controller.user;

import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.req.ChangePasswordDTO;
import com.socialmedia.clover_network.dto.req.ResetPasswordDTO;
import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.dto.req.UserSignUpReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.entity.TokenItem;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.service.AuthenticationService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

@RestController
@RequestMapping(path = "/api/authenticate")
public class AuthenticationController {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

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

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestParam(name = "tokenId") String tokenId, HttpServletRequest request) {
        try {
            ApiResponse res = authenticationService.logout(tokenId, request);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam(name = "email") String email) {
        try {
            ApiResponse res = authenticationService.generateOTP(email);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordDTO req) {
        try {
            ApiResponse res = authenticationService.resetPassword(req);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@RequestBody ChangePasswordDTO req) {
        try {
            ApiResponse res = authenticationService.changePassword(req);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
