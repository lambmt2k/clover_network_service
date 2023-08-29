package com.socialmedia.clover_network.controller;

import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.entity.TokenItem;
import com.socialmedia.clover_network.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/authenticate")
public class AuthenticationController {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login-by-email")
    public ResponseEntity<TokenItem> loginByOfficeOld(HttpServletRequest request, @RequestBody UserLoginReq req) {
        logger.info("Start login by email");

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
}
