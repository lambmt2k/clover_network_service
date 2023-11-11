package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.BaseProfile;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.entity.UserInfo;

import java.util.List;
import java.util.Map;


public interface UserService {
    UserInfo getUserInfo(String userId);
    Map<String, BaseProfile> multiGetBaseProfileByUserIds(List<String> userIds);
    BaseProfile mapUserInfoToBaseProfile(UserInfo userInfo);
    ApiResponse getUserInfo();
}
