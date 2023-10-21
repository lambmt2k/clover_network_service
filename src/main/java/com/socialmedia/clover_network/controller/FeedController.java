package com.socialmedia.clover_network.controller;

import com.socialmedia.clover_network.dto.res.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/feed")
public class FeedController {

    @GetMapping(value = "/list-user-home-old", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ApiResponse> listFeedUserHome(@RequestParam(name = "offset") int offset,
                                                        @RequestParam(name = "limit") int limit) {
        return null;
    }
}
