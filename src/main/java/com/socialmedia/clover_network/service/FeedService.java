package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.res.ApiResponse;

public interface FeedService {
    ApiResponse post(FeedItem feedItem);

    boolean checkUserCanPostFeedToGroup(String groupId, String userId);
}
