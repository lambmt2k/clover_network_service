package com.socialmedia.clover_network.controller.user;

import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.enumuration.Gender;
import com.socialmedia.clover_network.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@RequestMapping(path = "/api/user")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userService;

    @GetMapping("/get-user-info")
    public ResponseEntity<ApiResponse> getInfo(){
        try {
            ApiResponse res = userService.getUserInfo();
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/edit-profile")
    public ResponseEntity<ApiResponse> editProfile(@RequestParam(name = "firstname", required = false) String firstname,
                                                   @RequestParam(name = "lastname", required = false) String lastname,
                                                   @RequestParam(name = "phoneNo", required = false) String phoneNo,
                                                   @RequestParam(name = "gender", required = false) Gender gender,
                                                   @RequestParam(name = "dayOfBirth", required = false) Date dayOfBirth) {
        try {
            ApiResponse res = userService.editProfile(firstname, lastname, phoneNo, gender, dayOfBirth);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/change-user-avatar")
    public ResponseEntity<ApiResponse> changeUserAvatar(@RequestPart MultipartFile imageFile) {
        try {
            ApiResponse res = userService.changeUserAvatar(imageFile);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/change-user-banner")
    public ResponseEntity<ApiResponse> changeUserBanner(@RequestPart MultipartFile bannerFile) {
        try {
            ApiResponse res = userService.changeUserBanner(bannerFile);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
