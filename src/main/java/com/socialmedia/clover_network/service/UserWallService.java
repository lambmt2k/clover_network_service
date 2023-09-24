package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.entity.UserInfo;

public interface UserWallService {
    String createUserWall(String userId);
    boolean hasUserWall(String userId, UserInfo userInfo);
}
