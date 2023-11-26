package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FeedService {
    ApiResponse post(FeedItem feedItem, List<MultipartFile> files);
    ApiResponse listFeed(int limit, int offset);

    boolean checkUserCanPostFeedToGroup(String groupId, String userId);
}
