package com.socialmedia.clover_network.controller;

import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import com.socialmedia.clover_network.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/feed")
public class FeedController {

    private final Logger logger = LoggerFactory.getLogger(FeedController.class);

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping(value = "/list-user-home", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ApiResponse> listFeedUserHome(@RequestParam(name = "offset") int offset,
                                                        @RequestParam(name = "limit") int limit) {
        try {
            ApiResponse res = feedService.listFeed(limit, offset);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/post")
    public ResponseEntity<ApiResponse> postFeed(@RequestBody FeedItem feedItem) {
        try {
            ApiResponse res = feedService.post(feedItem);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> postFeed(@RequestParam(name = "postId") String postId) {
        return null;
    }

}
