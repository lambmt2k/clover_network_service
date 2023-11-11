package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.UserInfo;

public interface UserWallService {
    String createUserWall(String userId);
    boolean hasUserWall(String userId, UserInfo userInfo);
    boolean isUserWall(String groupId);
    GroupEntity getUserWallByUserId(String userId);
}
