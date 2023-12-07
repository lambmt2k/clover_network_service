package com.socialmedia.clover_network.controller.admin;

import com.socialmedia.clover_network.controller.user.FeedController;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.entity.TokenItem;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.service.admin.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/admin")
public class AdminController {

    @Autowired
    AdminService adminService;

    private final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @GetMapping(value = "/get-token/{userId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<TokenItem> getTokenByEmail(@PathVariable("userId") String userId) {
        try {
            TokenItem token = adminService.getTokenByUserId(userId);
            return ResponseEntity.ok().body(token);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping(value = "/get-user-info/{email}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UserInfo> getUserInfoByEmail(@PathVariable("email") String email) {
        try {
            UserInfo userInfo = adminService.getUserInfoByEmail(email);
            return ResponseEntity.ok().body(userInfo);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
