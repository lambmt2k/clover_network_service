package com.socialmedia.clover_network.controller;

import com.socialmedia.clover_network.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(path = "/api/images")
public class ImageController {
    @Autowired
    FirebaseService firebaseService;

    @GetMapping("/get-image-url")
    public ResponseEntity<String> getImageUrl(@RequestParam("imagePath") String imagePath){
        String imageUrl = firebaseService.getImageUrl(imagePath);
        return ResponseEntity.ok(imageUrl);
    }
}
