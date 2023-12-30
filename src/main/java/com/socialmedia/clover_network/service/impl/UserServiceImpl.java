package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.BaseProfile;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.UserInfoRes;
import com.socialmedia.clover_network.dto.res.UserProfileDTO;
import com.socialmedia.clover_network.entity.Connection;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.Gender;
import com.socialmedia.clover_network.enumuration.ImageType;
import com.socialmedia.clover_network.enumuration.UserStatus;
import com.socialmedia.clover_network.mapper.UserInfoMapper;
import com.socialmedia.clover_network.repository.ConnectionRepository;
import com.socialmedia.clover_network.repository.GroupMemberRepository;
import com.socialmedia.clover_network.repository.GroupRepository;
import com.socialmedia.clover_network.repository.UserInfoRepository;
import com.socialmedia.clover_network.service.FirebaseService;
import com.socialmedia.clover_network.service.GroupService;
import com.socialmedia.clover_network.service.UserService;
import com.socialmedia.clover_network.service.UserWallService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserInfoRepository userInfoRepository;
    @Autowired
    ConnectionRepository connectionRepository;

    @Autowired
    GroupService groupService;
    @Autowired
    UserWallService userWallService;
    @Autowired
    FirebaseService firebaseService;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private GroupRepository groupRepository;


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
    public BaseProfile getBaseProfileByUserId(String userId) {
        BaseProfile baseProfile = new BaseProfile();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        UserInfo userInfo = userInfoRepository.findByUserId(userId).orElse(null);
        if (userInfo != null) {
            baseProfile.setUserId(userInfo.getUserId());
            baseProfile.setDisplayName(userInfo.getFirstname() + CommonRegex.REGEX_SPACE + userInfo.getLastname());
            String imageUrlPublic = firebaseService.getImagePublicUrl(userInfo.getAvatarImgUrl());
            baseProfile.setAvatarImgUrl(imageUrlPublic);
            baseProfile.setPhoneNo(userInfo.getPhoneNo());
            baseProfile.setEmail(userInfo.getEmail());
            GroupEntity userWall = userWallService.getUserWallByUserId(userId);
            baseProfile.setUserWallId(userWall.getGroupId());
            Connection checkConnectAtoB = connectionRepository.findByUserIdAndUserIdConnected(currentUserId, userId);
            if (checkConnectAtoB != null && checkConnectAtoB.isConnectStatus()) {
                baseProfile.setConnected(true);
            }
        }
        return baseProfile;
    }

    @Override
    public BaseProfile mapUserInfoToBaseProfile(UserInfo userInfo) {
        BaseProfile baseProfile = new BaseProfile();
        if (userInfo != null) {
            baseProfile.setUserId(userInfo.getUserId());
            baseProfile.setDisplayName(userInfo.getFirstname() + CommonRegex.REGEX_SPACE + userInfo.getLastname());
            String imageUrlPublic = firebaseService.getImagePublicUrl(userInfo.getAvatarImgUrl());
            baseProfile.setAvatarImgUrl(imageUrlPublic);
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
                String imageUrlPublic = firebaseService.getImagePublicUrl(userInfo.getAvatarImgUrl());
                data.setAvatar(imageUrlPublic);
                data.setPhoneNo(userInfo.getPhoneNo());
                data.setGender(userInfo.getGender().equals(Gender.MALE) ? "MALE"
                        : (userInfo.getGender().equals(Gender.FEMALE) ? "FEMALE" : "OTHER"));
                data.setUserRole(userInfo.getUserRole().getRoleName());
                data.setDayOfBirth(dateFormat.format(userInfo.getDayOfBirth()));
                data.setStatus(userInfo.getStatus().getStatusName());
                //get user's wall info
                GroupEntity userWall = userWallService.getUserWallByUserId(currentUserId);
                data.setUserWallId(userWall.getGroupId());
                String bannerUrl = firebaseService.getImagePublicUrl(userWall.getBannerImgUrl());
                data.setBannerUrl(bannerUrl);
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
    public ApiResponse getUserProfile(String userId) {
        logger.info("Start [getUserProfile]");
        ApiResponse res = new ApiResponse();
        SimpleDateFormat dateFormat = new SimpleDateFormat(CommonRegex.PATTERN_DATE.pattern());
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (currentUserId != null) {
            logger.info("Get info of userId: {}", userId);
            Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(userId);
            if (userInfoOpt.isPresent()) {
                UserInfo userInfo = userInfoOpt.get();
                UserProfileDTO userProfileDTO = new UserProfileDTO();
                UserInfoRes data = new UserInfoRes();
                data.setUserId(userInfo.getUserId());
                data.setEmail(userInfo.getEmail());
                data.setFirstname(userInfo.getFirstname());
                data.setLastname(userInfo.getLastname());
                String imageUrlPublic = firebaseService.getImagePublicUrl(userInfo.getAvatarImgUrl());
                data.setAvatar(imageUrlPublic);
                data.setPhoneNo(userInfo.getPhoneNo());
                data.setGender(userInfo.getGender().equals(Gender.MALE) ? "MALE"
                        : (userInfo.getGender().equals(Gender.FEMALE) ? "FEMALE" : "OTHER"));
                data.setUserRole(userInfo.getUserRole().getRoleName());
                data.setDayOfBirth(dateFormat.format(userInfo.getDayOfBirth()));
                data.setStatus(userInfo.getStatus().getStatusName());
                //get user's wall info
                GroupEntity userWall = userWallService.getUserWallByUserId(userId);
                data.setUserWallId(userWall.getGroupId());
                String bannerUrl = firebaseService.getImagePublicUrl(userWall.getBannerImgUrl());
                data.setBannerUrl(bannerUrl);
                Connection checkConnectAtoB = connectionRepository.findByUserIdAndUserIdConnected(currentUserId, userId);
                if (checkConnectAtoB != null && checkConnectAtoB.isConnectStatus()) {
                    data.setConnected(true);
                }
                userProfileDTO.setUserInfo(data);
                List<Connection> listUserConnect = connectionRepository.findByUserIdAndConnectStatusTrue(userId);
                userProfileDTO.setTotalConnect(listUserConnect.size());

                List<Connection> listUserConnector = connectionRepository.findByUserIdConnectedAndConnectStatusTrue(userId);
                userProfileDTO.setTotalConnector(listUserConnector.size());

                res.setCode(ErrorCode.User.ACTION_SUCCESS.getCode());
                res.setData(userProfileDTO);
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
        logger.info("End api [getUserProfile]");
        return res;

    }

    @Override
    public ApiResponse getListUserConnect(String userId, int page, int size) {
        logger.info("Start [getListUserConnect]");
        ApiResponse res = new ApiResponse();
        SimpleDateFormat dateFormat = new SimpleDateFormat(CommonRegex.PATTERN_DATE.pattern());
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        List<Connection> listUserConnector = connectionRepository.findByUserIdConnectedAndConnectStatusTrue(userId);
        if (listUserConnector.isEmpty()) {
            res.setCode(ErrorCode.User.LIST_NO_USER.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.User.LIST_NO_USER.getMessageEN());
            res.setMessageVN(ErrorCode.User.LIST_NO_USER.getMessageVN());
            return res;
        }
        UserProfileDTO.ListUserConnectDTO data = new UserProfileDTO.ListUserConnectDTO();
        List<BaseProfile> listBaseProfileConnect = new ArrayList<>();

        listUserConnector.forEach(user -> listBaseProfileConnect.add(this.getBaseProfileByUserId(user.getUserId())));
        List<BaseProfile> userProfiles = new ArrayList<>();
        if (!listBaseProfileConnect.isEmpty() && listBaseProfileConnect.size() > size) {
            userProfiles = listBaseProfileConnect
                    .stream()
                    .sorted(Comparator.comparing(BaseProfile::getDisplayName))
                    .skip((long) (page - 1) * size)
                    .limit(size)
                    .collect(Collectors.toList());
        } else {
            userProfiles = listBaseProfileConnect;
        }

        data.setTotal(listUserConnector.size());
        data.setUserProfiles(userProfiles);

        res.setCode(ErrorCode.User.ACTION_SUCCESS.getCode());
        res.setData(data);
        res.setMessageEN(ErrorCode.User.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.User.ACTION_SUCCESS.getMessageVN());
        logger.info("End api [getListUserConnect]");
        return res;
    }

    @Override
    public ApiResponse getListUserConnector(String userId, int page, int size) {
        logger.info("Start [getListUserConnector]");
        ApiResponse res = new ApiResponse();
        SimpleDateFormat dateFormat = new SimpleDateFormat(CommonRegex.PATTERN_DATE.pattern());
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        List<Connection> listUserConnect = connectionRepository.findByUserIdAndConnectStatusTrue(userId);
        if (listUserConnect.isEmpty()) {
            res.setCode(ErrorCode.User.LIST_NO_USER.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.User.LIST_NO_USER.getMessageEN());
            res.setMessageVN(ErrorCode.User.LIST_NO_USER.getMessageVN());
            return res;
        }
        UserProfileDTO.ListUserConnectDTO data = new UserProfileDTO.ListUserConnectDTO();
        List<BaseProfile> listBaseProfileConnect = new ArrayList<>();

        listUserConnect.forEach(user -> listBaseProfileConnect.add(this.getBaseProfileByUserId(user.getUserIdConnected())));
        List<BaseProfile> userProfiles = new ArrayList<>();
        if (!listBaseProfileConnect.isEmpty() && listBaseProfileConnect.size() > size) {
            userProfiles = listBaseProfileConnect
                    .stream()
                    .sorted(Comparator.comparing(BaseProfile::getDisplayName))
                    .skip((long) (page - 1) * size)
                    .limit(size)
                    .collect(Collectors.toList());
        } else {
            userProfiles = listBaseProfileConnect;
        }

        data.setTotal(listUserConnect.size());
        data.setUserProfiles(userProfiles);

        res.setCode(ErrorCode.User.ACTION_SUCCESS.getCode());
        res.setData(data);
        res.setMessageEN(ErrorCode.User.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.User.ACTION_SUCCESS.getMessageVN());

        logger.info("End api [getListUserConnector]");
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

    @Override
    public ApiResponse changeUserAvatar(MultipartFile imageFile) {
        logger.info("Start API [changeUserAvatar]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        if (imageFile.isEmpty() || !isImage(imageFile)) {
            res.setCode(ErrorCode.User.INVALID_IMAGE_FILE.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.User.INVALID_IMAGE_FILE.getMessageEN());
            res.setMessageVN(ErrorCode.User.INVALID_IMAGE_FILE.getMessageVN());
            return res;
        }

        Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(currentUserId);
        if (userInfoOpt.isEmpty() || !userInfoOpt.get().getStatus().equals(UserStatus.ACTIVE)) {
            res.setCode(ErrorCode.User.PROFILE_GET_EMPTY.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageEN());
            res.setMessageVN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageVN());
            return res;
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            SimpleDateFormat dateFormat = new SimpleDateFormat(CommonRegex.PATTERN_DATE.pattern());
            UserInfo userInfo = userInfoOpt.get();
            String imageFbUrl = firebaseService.uploadImage(imageFile, ImageType.USER_AVATAR);
            userInfo.setAvatarImgUrl(imageFbUrl);
            userInfo.setUpdatedBy(currentUserId);
            userInfo.setUpdatedDate(now);
            userInfoRepository.save(userInfo);

            UserInfoRes data = UserInfoMapper.INSTANCE.toDTO(userInfo);
            String imageUrlPublic = firebaseService.getImagePublicUrl(userInfo.getAvatarImgUrl());
            data.setAvatar(imageUrlPublic);
            data.setGender(userInfo.getGender().equals(Gender.MALE) ? "MALE"
                    : (userInfo.getGender().equals(Gender.FEMALE) ? "FEMALE" : "OTHER"));
            data.setUserRole(userInfo.getUserRole().getRoleName());
            data.setDayOfBirth(dateFormat.format(userInfo.getDayOfBirth()));
            data.setStatus(userInfo.getStatus().getStatusName());
            //get user's wall info
            GroupEntity userWall = userWallService.getUserWallByUserId(currentUserId);
            data.setUserWallId(userWall.getGroupId());
            String bannerUrl = firebaseService.getImagePublicUrl(userWall.getBannerImgUrl());
            data.setBannerUrl(bannerUrl);
            res.setCode(ErrorCode.User.ACTION_SUCCESS.getCode());
            res.setData(data);
            res.setMessageEN(ErrorCode.User.ACTION_SUCCESS.getMessageEN());
            res.setMessageVN(ErrorCode.User.ACTION_SUCCESS.getMessageVN());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.info("End API [changeUserAvatar]");
        return res;
    }

    private boolean isImage(MultipartFile file) {
        // Get the file name
        String fileName = file.getOriginalFilename();

        // Check if the file has a valid image extension (you can extend this list)
        return fileName != null && (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"));
    }

    @Override
    public ApiResponse changeUserBanner(MultipartFile bannerFile) {
        logger.info("Start API [changeUserBanner]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        if (bannerFile.isEmpty() || !isImage(bannerFile)) {
            res.setCode(ErrorCode.User.INVALID_IMAGE_FILE.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.User.INVALID_IMAGE_FILE.getMessageEN());
            res.setMessageVN(ErrorCode.User.INVALID_IMAGE_FILE.getMessageVN());
            return res;
        }

        Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(currentUserId);
        if (userInfoOpt.isEmpty() || !userInfoOpt.get().getStatus().equals(UserStatus.ACTIVE)) {
            res.setCode(ErrorCode.User.PROFILE_GET_EMPTY.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageEN());
            res.setMessageVN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageVN());
            return res;
        }
        LocalDateTime now = LocalDateTime.now();
        SimpleDateFormat dateFormat = new SimpleDateFormat(CommonRegex.PATTERN_DATE.pattern());
        UserInfo userInfo = userInfoOpt.get();
        userInfo.setUpdatedBy(currentUserId);
        userInfo.setUpdatedDate(now);
        userInfoRepository.save(userInfo);

        UserInfoRes data = UserInfoMapper.INSTANCE.toDTO(userInfo);
        String imageUrlPublic = firebaseService.getImagePublicUrl(userInfo.getAvatarImgUrl());
        data.setAvatar(imageUrlPublic);
        data.setGender(userInfo.getGender().equals(Gender.MALE) ? "MALE"
                : (userInfo.getGender().equals(Gender.FEMALE) ? "FEMALE" : "OTHER"));
        data.setUserRole(userInfo.getUserRole().getRoleName());
        data.setDayOfBirth(dateFormat.format(userInfo.getDayOfBirth()));
        data.setStatus(userInfo.getStatus().getStatusName());
        //get user's wall info
        GroupEntity userWall = userWallService.getUserWallByUserId(currentUserId);
        data.setUserWallId(userWall.getGroupId());
        String bannerUrl = firebaseService.getImagePublicUrl(userWall.getBannerImgUrl());
        data.setBannerUrl(bannerUrl);
        res.setCode(ErrorCode.User.ACTION_SUCCESS.getCode());
        res.setData(data);
        res.setMessageEN(ErrorCode.User.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.User.ACTION_SUCCESS.getMessageVN());

        logger.info("End API [changeUserBanner]");
        return res;
    }


}
