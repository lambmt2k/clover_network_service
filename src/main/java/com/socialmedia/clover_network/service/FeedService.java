package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.CommentDTO;
import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.ReactDTO;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FeedService {
    ApiResponse post(FeedItem feedItem, List<MultipartFile> images);
    ApiResponse listFeedUserHome(int limit, int offset);
    ApiResponse listFeedUserHomeV2(int page, int size);
    ApiResponse listFeedGroupHome(String groupId, int page, int size);
    ApiResponse listFeedAllGroupHome(int page, int size);
    boolean checkUserCanPostFeedToGroup(String groupId, String userId);
    ApiResponse commentToFeed(CommentDTO commentDTO);
    ApiResponse reactToFeed(ReactDTO reactDTO);
}
