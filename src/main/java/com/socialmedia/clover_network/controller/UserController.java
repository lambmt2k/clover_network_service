package com.socialmedia.clover_network.controller;

import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/get-user-info")
    public ResponseEntity<ApiResponse> getInfo(){
        ApiResponse res = userService.getUserInfo();
        return ResponseEntity.ok(res);
    }
}
