package com.socialmedia.clover_network.controller;

import com.socialmedia.clover_network.service.UserService;
import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.dto.res.UserLoginRes;
import com.socialmedia.clover_network.util.ResponseDataUtil;
import com.socialmedia.clover_network.util.SystemException;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {
    private UserService service;

    @GetMapping(name = "/login")
    public ResponseDataUtil<UserLoginRes> login(@Validated @RequestBody UserLoginReq req) {
        try {
            UserLoginRes res = service.login(req);
            return ResponseDataUtil.ok(res);
        } catch (SystemException ex) {
            return ResponseDataUtil.error(ex.getRc());
        }
    }
}
