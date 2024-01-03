package com.socialmedia.clover_network.controller.user;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.dto.FirebaseTokenUserItem;
import com.socialmedia.clover_network.service.FirebaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/firebase")
public class FirebaseController {

    @Autowired
    FirebaseService firebaseService;

    @GetMapping("/put-device-token")
    public ResponseEntity<String> putDeviceToken(@RequestParam(name = "deviceToken") String deviceToken) {
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (!StringUtils.isEmpty(currentUserId)) {
            FirebaseTokenUserItem item = FirebaseTokenUserItem.builder().userId(currentUserId).token(deviceToken).build();
            firebaseService.pushDeviceToken(item);
            return ResponseEntity.ok("Success");
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/remove-device-token")
    public ResponseEntity<String> removeDeviceToken(@RequestParam(name = "deviceToken") String deviceToken) {
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (!StringUtils.isEmpty(currentUserId)) {
            FirebaseTokenUserItem item = FirebaseTokenUserItem.builder().userId(currentUserId).token(deviceToken).build();
            firebaseService.removeDeviceToken(item);
            return ResponseEntity.ok("Success");
        }
        return ResponseEntity.badRequest().build();
    }
}
