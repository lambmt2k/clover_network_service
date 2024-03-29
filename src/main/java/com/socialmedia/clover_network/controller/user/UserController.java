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

    @GetMapping("/get-user-profile/{userId}")
    public ResponseEntity<ApiResponse> getUserProfile(@PathVariable String userId){
        try {
            ApiResponse res = userService.getUserProfile(userId);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/get-list-friend")
    public ResponseEntity<ApiResponse> getListFriend(@RequestParam(name = "page") int page,
                                                     @RequestParam(name = "size") int size){
        try {
            ApiResponse res = userService.getListFriend(page, size);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/get-list-friend-request")
    public ResponseEntity<ApiResponse> getListFriendRequest(){
        try {
            ApiResponse res = userService.getListFriendRequest();
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

    @GetMapping("/get-list-connect")
    public ResponseEntity<ApiResponse> getListUserConnect(@RequestParam(name = "userId") String userId,
                                                          @RequestParam(name = "page") int page,
                                                          @RequestParam(name = "size") int size){
        try {
            ApiResponse res = userService.getListUserConnect(userId, page, size);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/get-list-recommend")
    public ResponseEntity<ApiResponse> getListUserRecommend(){
        try {
            ApiResponse res = userService.getListUserRecommend();
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/get-list-connector")
    public ResponseEntity<ApiResponse> getListUserConnector(@RequestParam(name = "userId") String userId,
                                                            @RequestParam(name = "page") int page,
                                                            @RequestParam(name = "size") int size){
        try {
            ApiResponse res = userService.getListUserConnector(userId, page, size);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
