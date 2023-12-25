package com.socialmedia.clover_network.controller.user;

import com.socialmedia.clover_network.dto.CommentDTO;
import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.ReactDTO;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
            ApiResponse res = feedService.listFeedUserHome(limit, offset);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping(value = "/list-user-home-v2", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ApiResponse> listFeedUserHomeV2(@RequestParam(name = "page") int page,
                                                        @RequestParam(name = "size") int size) {
        try {
            ApiResponse res = feedService.listFeedUserHomeV2(page, size);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping(value = "/list-group-home", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ApiResponse> listFeedGroupHome(@RequestParam(name = "groupId") String groupId,
                                                         @RequestParam(name = "page") int page,
                                                         @RequestParam(name = "size") int size) {
        try {
            ApiResponse res = feedService.listFeedGroupHome(groupId, page, size);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/post")
    public ResponseEntity<ApiResponse> postFeed(@RequestPart(name = "feedItem") FeedItem feedItem,
                                                @RequestPart(name = "images", required = false)List<MultipartFile> images) {
        try {
            ApiResponse res = feedService.post(feedItem, images);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> detailFeed(@RequestParam(name = "postId") String postId) {
        return null;
    }

    @PostMapping("/comment")
    public ResponseEntity<ApiResponse> commentToFeed(@RequestBody CommentDTO commentDTO) {
        try {
            ApiResponse res = feedService.commentToFeed(commentDTO);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/react")
    public ResponseEntity<ApiResponse> reactToFeed(@RequestBody ReactDTO reactDTO) {
        try {
            ApiResponse res = feedService.reactToFeed(reactDTO);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
