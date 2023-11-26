package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.BaseProfile;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.UserInfoRes;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.Gender;
import com.socialmedia.clover_network.mapper.UserInfoMapper;
import com.socialmedia.clover_network.repository.UserInfoRepository;
import com.socialmedia.clover_network.service.GroupService;
import com.socialmedia.clover_network.service.UserService;
import com.socialmedia.clover_network.service.UserWallService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    GroupService groupService;
    @Autowired
    UserWallService userWallService;
    @Autowired
    UserInfoMapper userInfoMapper;


    @Override
    public UserInfo getUserInfo(String userId) {
        Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(userId);
        return userInfoOpt.orElse(null);
    }

    @Override
    public Map<String, BaseProfile> multiGetBaseProfileByUserIds(List<String> userIds) {
        logger.info("[multiGetBaseProfileByUserIds] Start get multi profile by userId");
        Map<String, BaseProfile> res = new HashMap<>();
        if (!CollectionUtils.isEmpty(userIds)) {
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

    @Override
    public ApiResponse getUserInfo() {
        logger.info("Start [getUserInfo]");
        ApiResponse res = new ApiResponse();
        SimpleDateFormat dateFormat = new SimpleDateFormat(CommonRegex.PATTERN_DATE.pattern());
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (currentUserId != null) {
            logger.info("Get info of userId: {}", currentUserId);
            Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(currentUserId);
            if (userInfoOpt.isPresent()) {
                UserInfo userInfo = userInfoOpt.get();
                UserInfoRes data = new UserInfoRes();
                data.setUserId(userInfo.getUserId());
                data.setEmail(userInfo.getEmail());
                data.setFirstname(userInfo.getFirstname());
                data.setLastname(userInfo.getLastname());
                data.setAvatarImgUrl(userInfo.getAvatarImgUrl());
                data.setPhoneNo(userInfo.getPhoneNo());
                data.setGender(userInfo.getGender().equals(Gender.MALE) ? "MALE"
                        : (userInfo.getGender().equals(Gender.FEMALE) ? "FEMALE" : "OTHER"));
                data.setUserRole(userInfo.getUserRole().getRoleName());
                data.setDayOfBirth(dateFormat.format(userInfo.getDayOfBirth()));
                data.setStatus(userInfo.getStatus().getStatusName());
                //get user's wall info
                GroupEntity userWall = userWallService.getUserWallByUserId(currentUserId);
                data.setUserWallId(userWall.getGroupId());

                res.setCode(ErrorCode.User.ACTION_SUCCESS.getCode());
                res.setData(data);
                res.setMessageEN(ErrorCode.User.ACTION_SUCCESS.getMessageEN());
                res.setMessageVN(ErrorCode.User.ACTION_SUCCESS.getMessageVN());
            } else {
                res.setCode(ErrorCode.User.PROFILE_GET_EMPTY.getCode());
                res.setData(null);
                res.setMessageEN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageEN());
                res.setMessageVN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageVN());
            }
        } else {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
        }
        logger.info("End api [getUserInfo]");
        return res;

    }

    @Override
    public ApiResponse editProfile(String firstname, String lastname, String phoneNo, Gender gender, Date dayOfBirth) {
        logger.info("Start API [editProfile]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        LocalDateTime now = LocalDateTime.now();
        SimpleDateFormat dateFormat = new SimpleDateFormat(CommonRegex.PATTERN_DATE.pattern());
        if (currentUserId != null) {
            Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(currentUserId);
            if (userInfoOpt.isPresent()) {
                UserInfo existedUser = userInfoOpt.get();
                if (StringUtils.isNotEmpty(firstname)) {
                    existedUser.setFirstname(firstname);
                }
                if (StringUtils.isNotEmpty(lastname)) {
                    existedUser.setLastname(lastname);
                }
                if (StringUtils.isNotEmpty(phoneNo)) {
                    existedUser.setPhoneNo(phoneNo);
                }
                if (Objects.nonNull(gender)) {
                    existedUser.setGender(gender);
                }
                if (Objects.nonNull(dayOfBirth)) {
                    existedUser.setDayOfBirth(dayOfBirth);
                }
                existedUser.setUpdatedBy(currentUserId);
                existedUser.setUpdatedDate(now);
                userInfoRepository.save(existedUser);

                UserInfoRes data = userInfoMapper.toDTO(existedUser);
                data.setGender(existedUser.getGender().equals(Gender.MALE) ? "MALE"
                        : (existedUser.getGender().equals(Gender.FEMALE) ? "FEMALE" : "OTHER"));
                data.setUserRole(existedUser.getUserRole().getRoleName());
                data.setDayOfBirth(dateFormat.format(existedUser.getDayOfBirth()));
                data.setStatus(existedUser.getStatus().getStatusName());
                //get user's wall info
                GroupEntity userWall = userWallService.getUserWallByUserId(currentUserId);
                data.setUserWallId(userWall.getGroupId());

                res.setCode(ErrorCode.User.ACTION_SUCCESS.getCode());
                res.setData(data);
                res.setMessageEN(ErrorCode.User.ACTION_SUCCESS.getMessageEN());
                res.setMessageVN(ErrorCode.User.ACTION_SUCCESS.getMessageVN());
            } else {
                res.setCode(ErrorCode.User.PROFILE_GET_EMPTY.getCode());
                res.setData(null);
                res.setMessageEN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageEN());
                res.setMessageVN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageVN());
            }
        } else {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
        }
        logger.info("End API [editProfile]");
        return res;
    }


}
