package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.dto.BaseProfile;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.repository.UserInfoRepository;
import com.socialmedia.clover_network.service.GroupService;
import com.socialmedia.clover_network.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    GroupService groupService;


    @Override
    public UserInfo getUserInfo(String userId) {
        Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(userId);
        return userInfoOpt.orElse(null);
    }

    @Override
    public Map<String, BaseProfile> multiGetBaseProfileByUserIds(List<String> userIds) {
        logger.info("[multiGetBaseProfileByUserIds] Start get multi profile by userId");
        Map<String, BaseProfile> res =new HashMap<>();
        if(!CollectionUtils.isEmpty(userIds)) {
            List<UserInfo> userInfos = userInfoRepository.findByUserIdIn(userIds);
            if (!CollectionUtils.isEmpty(userInfos)) {
                userInfos.forEach(user -> {
                    BaseProfile baseProfile = this.mapUserInfoToBaseProfile(user);
                    if (baseProfile != null && StringUtils.isNotEmpty(baseProfile.getUserId())) {
                        res.put(baseProfile.getUserId(), baseProfile);
                    }
                });

                //fill userWallId of each member
                Map<String, String> mapUserWallId = groupService.getUserWallIdByUserId(userIds);
                for (Map.Entry<String, String> entry : mapUserWallId.entrySet()) {
                    String userId = entry.getKey();
                    String userWallId = entry.getValue();
                    res.get(userId).setUserWallId(userWallId);
                }
            }
        }
        return res;
    }

    @Override
    public BaseProfile mapUserInfoToBaseProfile(UserInfo userInfo) {
        BaseProfile baseProfile = new BaseProfile();
        if (userInfo != null) {
            baseProfile.setUserId(userInfo.getUserId());
            baseProfile.setDisplayName(userInfo.getDisplayName());
            baseProfile.setAvatarImgUrl(userInfo.getAvatarImgUrl());
            baseProfile.setPhoneNo(userInfo.getPhoneNo());
            baseProfile.setEmail(userInfo.getEmail());
        }
        return baseProfile;
    }


}
