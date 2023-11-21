package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.BaseProfile;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.Gender;

import java.util.Date;
import java.util.List;
import java.util.Map;


public interface UserService {
    UserInfo getUserInfo(String userId);
    Map<String, BaseProfile> multiGetBaseProfileByUserIds(List<String> userIds);
    BaseProfile mapUserInfoToBaseProfile(UserInfo userInfo);
    ApiResponse getUserInfo();
    ApiResponse editProfile(String firstname, String lastname, String phoneNo, Gender gender, Date dayOfBirth);
}
