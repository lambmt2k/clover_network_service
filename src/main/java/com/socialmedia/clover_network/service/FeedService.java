package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.CommentDTO;
import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.res.ApiResponse;

public interface FeedService {
    ApiResponse post(FeedItem feedItem);
    ApiResponse listFeed(int limit, int offset);
    boolean checkUserCanPostFeedToGroup(String groupId, String userId);
    ApiResponse commentToFeed(CommentDTO commentDTO);
}
