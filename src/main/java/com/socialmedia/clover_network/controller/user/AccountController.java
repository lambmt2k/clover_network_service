package com.socialmedia.clover_network.controller.user;

import com.socialmedia.clover_network.constant.CommonConstant;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.service.AuthenticationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {

    private final AuthenticationService authenticationService;

    public AccountController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/verify-account")
    public String verifyAccount(@RequestParam("tokenId") String tokenId){
        ApiResponse res = authenticationService.verifyAccount(tokenId);
        if (res.getCode() == ErrorCode.User.ACTION_SUCCESS.getCode()) {
            return  "redirect:" + CommonConstant.LOGIN_PAGE_URL;
        } else return res.getMessageVN();
    }
}
