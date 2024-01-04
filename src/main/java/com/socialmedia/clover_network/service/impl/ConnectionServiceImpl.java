package com.socialmedia.clover_network.service.impl;

import com.google.gson.Gson;
import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.BaseProfile;
import com.socialmedia.clover_network.dto.ConnectionDTO;
import com.socialmedia.clover_network.dto.NotificationItem;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.UserInfoRes;
import com.socialmedia.clover_network.entity.*;
import com.socialmedia.clover_network.enumuration.UserStatus;
import com.socialmedia.clover_network.mapper.ConnectionMapper;
import com.socialmedia.clover_network.mapper.UserInfoMapper;
import com.socialmedia.clover_network.repository.*;
import com.socialmedia.clover_network.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConnectionServiceImpl implements ConnectionService {

    private final Logger logger = LoggerFactory.getLogger(ConnectionServiceImpl.class);
    private static Gson gson = new Gson();
    @Autowired
    UserService userService;
    @Autowired
    UserWallService userWallService;
    @Autowired
    GroupService groupService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    ConnectionRepository connectionRepository;
    @Autowired
    GroupMemberRepository groupMemberRepository;
    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    FeedGroupRepository feedGroupDAO;
    @Autowired
    FeedUserRepository feedUserDAO;
    @Autowired
    ConnectionMapper connectionMapper;


    @Override
    @Transactional
    public ApiResponse connectToUser(ConnectionDTO.ConnectUserItem req) {
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        if (currentUserId.equals(req.getTargetUserId())) {
            res.setCode(ErrorCode.Connection.CANNOT_CONNECT_YOURSELF.getCode());
            res.setData(req);
            res.setMessageEN(ErrorCode.Connection.CANNOT_CONNECT_YOURSELF.getMessageEN());
            res.setMessageVN(ErrorCode.Connection.CANNOT_CONNECT_YOURSELF.getMessageVN());
            return res;
        }
        UserInfo targetUserInfo = userService.getUserInfo(req.getTargetUserId());
        if (Objects.isNull(targetUserInfo) || !targetUserInfo.getStatus().equals(UserStatus.ACTIVE)) {
            res.setCode(ErrorCode.User.PROFILE_GET_EMPTY.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageEN());
            res.setMessageVN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageVN());
            return res;
        }
        //check connection currentUser=A and targetUser=B
        Connection checkConnectAtoB = connectionRepository.findByUserIdAndUserIdConnected(currentUserId, req.getTargetUserId());
        Connection checkConnectBtoA = connectionRepository.findByUserIdAndUserIdConnected(req.getTargetUserId(), currentUserId);

        if (req.isStatus()
                && (Objects.nonNull(checkConnectAtoB) && checkConnectAtoB.isConnectStatus())) {
            UserInfoRes targetUserInfoRes = UserInfoMapper.INSTANCE.toDTO(targetUserInfo);
            targetUserInfoRes.setConnected(true);

            res.setCode(ErrorCode.Connection.ALREADY_CONNECTED.getCode());
            res.setData(targetUserInfoRes);
            res.setMessageEN(ErrorCode.Connection.ALREADY_CONNECTED.getMessageEN());
            res.setMessageVN(ErrorCode.Connection.ALREADY_CONNECTED.getMessageVN());
            return res;
        }
        GroupEntity targetUserWall = userWallService.getUserWallByUserId(req.getTargetUserId());
        LocalDateTime now = LocalDateTime.now();
        if (checkConnectAtoB != null) {
            if (req.isStatus()) { //case connect
                Optional<GroupMember> groupMemberOpt = groupMemberRepository.findByUserIdAndGroupId(currentUserId, targetUserWall.getGroupId());
                if (groupMemberOpt.isPresent() && groupMemberOpt.get().isDelFlag()) {
                    GroupMember groupMember = groupMemberOpt.get();
                    groupMember.setDelFlag(false);
                    groupMember.setJoinTime(now);
                    groupMemberRepository.save(groupMember);
                }
                if (groupMemberOpt.isEmpty()) {
                    if (targetUserWall.getGroupPrivacy().equals(GroupEntity.GroupPrivacy.PUBLIC)) {
                        //add userid to member group
                        groupService.addMemberList(targetUserWall.getGroupId(), currentUserId, false);
                    } else if (targetUserWall.getGroupPrivacy().equals(GroupEntity.GroupPrivacy.PRIVATE)) {
                        //add userid to list waiting
                        groupService.addWaitingMemberList(targetUserWall.getGroupId(), currentUserId);
                    }
                }
                checkConnectAtoB.setConnectStatus(true);
                checkConnectAtoB.setTimeConnect(now);
                Connection success = connectionRepository.save(checkConnectAtoB);

                //this.broadcastConnect(connectionMapper.toDTO(success), checkConnectBtoA != null && checkConnectBtoA.isConnectStatus());
            } else { //case disconnect
                Optional<GroupMember> groupMemberOpt = groupMemberRepository.findByUserIdAndGroupId(currentUserId, targetUserWall.getGroupId());
                if (groupMemberOpt.isPresent() && !groupMemberOpt.get().isDelFlag()) {
                    GroupMember groupMember = groupMemberOpt.get();
                    groupMember.setDelFlag(true);
                    groupMember.setLeaveTime(now);
                    groupMemberRepository.save(groupMember);
                }
                checkConnectAtoB.setConnectStatus(false);
                checkConnectAtoB.setTimeDisconnect(now);
                connectionRepository.save(checkConnectAtoB);

                //remove feedGroup in feedUser
                FeedGroupEntity feedGroupEntity = feedGroupDAO.findById(targetUserWall.getGroupId()).orElse(null);
                if (feedGroupEntity != null) {
                    List<String> feedGroupIds = feedGroupEntity.getListFeedId();

                    FeedUserEntity feedUserEntity = feedUserDAO.findById(currentUserId).orElse(null);
                    if (feedUserEntity != null) {
                        List<String> feedIds = feedUserEntity.getListFeedId();
                        feedIds.removeAll(feedGroupIds);
                        feedIds = feedIds.stream().distinct().collect(Collectors.toList());
                        feedUserEntity.setValue(gson.toJson(feedIds));
                        feedUserDAO.save(feedUserEntity);
                    }
                }

            }
            res.setCode(ErrorCode.Connection.ACTION_SUCCESS.getCode());
            res.setData(targetUserInfo.getUserId());
            res.setMessageEN(ErrorCode.Connection.ACTION_SUCCESS.getMessageEN());
            res.setMessageVN(ErrorCode.Connection.ACTION_SUCCESS.getMessageVN());
        } else {
            if (req.isStatus()) {
                Optional<GroupMember> groupMemberOpt = groupMemberRepository.findByUserIdAndGroupId(currentUserId, targetUserWall.getGroupId());
                if (groupMemberOpt.isPresent() && groupMemberOpt.get().isDelFlag()) {
                    GroupMember groupMember = groupMemberOpt.get();
                    groupMember.setDelFlag(false);
                    groupMember.setJoinTime(now);
                    groupMemberRepository.save(groupMember);
                }
                if (groupMemberOpt.isEmpty()) {
                    if (targetUserWall.getGroupPrivacy().equals(GroupEntity.GroupPrivacy.PUBLIC)) {
                        //add userid to member group
                        groupService.addMemberList(targetUserWall.getGroupId(), currentUserId, false);
                    } else if (targetUserWall.getGroupPrivacy().equals(GroupEntity.GroupPrivacy.PRIVATE)) {
                        //add userid to list waiting
                        groupService.addWaitingMemberList(targetUserWall.getGroupId(), currentUserId);
                    }
                }
                Connection newConnection = Connection.builder()
                        .userId(currentUserId)
                        .userIdConnected(req.getTargetUserId())
                        .connectStatus(true)
                        .timeConnect(now).build();
                Connection success = connectionRepository.save(newConnection);
                this.broadcastConnect(ConnectionMapper.INSTANCE.toDTO(success), checkConnectBtoA != null && checkConnectBtoA.isConnectStatus());
                res.setCode(ErrorCode.Connection.ACTION_SUCCESS.getCode());
                res.setData(targetUserInfo.getUserId());
                res.setMessageEN(ErrorCode.Connection.ACTION_SUCCESS.getMessageEN());
                res.setMessageVN(ErrorCode.Connection.ACTION_SUCCESS.getMessageVN());
            }
        }

        return res;
    }

    private void broadcastConnect(ConnectionDTO connectionDTO, boolean isConnectTogether) {
        logger.info("[broadcastConnect] Start broadcast connection: " + connectionDTO);
        Long notificationId = this.createNotificationConnect(connectionDTO, isConnectTogether);
        if (notificationId != null) {
            Map<String, String> customData = new HashMap<>();
            customData.put("isConnectTogether", isConnectTogether ? "true" : "false");
            notificationService.broadcastNotification(notificationId, Collections.singletonList(connectionDTO.getUserIdConnected()), customData);
        }
    }

    private Long createNotificationConnect(ConnectionDTO connectionDTO, boolean isConnectTogether) {
        logger.info("[createNotificationConnect] Start create notification of connection: " + connectionDTO);
        LocalDateTime now = LocalDateTime.now();
        BaseProfile fromUser = userService.getBaseProfileByUserId(connectionDTO.getUserId());
        BaseProfile toUser = userService.getBaseProfileByUserId(connectionDTO.getUserIdConnected());
        String message = "";
        if (!isConnectTogether) {
            message = fromUser.getDisplayName()
                    + " connected to you. Would you like to connect with "
                    + fromUser.getDisplayName();
        } else {
            message = fromUser.getDisplayName()
                    + "has connected back to you. Now everyone can post on each other's wall.";
        }
        String groupName = groupRepository.findByGroupIdAndDelFlagFalse(toUser.getUserWallId()).getGroupName();
        NotificationItem notiItem = NotificationItem.builder()
                .templateId(NotificationEntity.TemplateNotification.CONNECTION)
                .fromUserId(connectionDTO.getUserId())
                .toUserId(connectionDTO.getUserIdConnected())
                .username(fromUser.getDisplayName())
                .objectId(String.valueOf(connectionDTO.getId()))
                .message(message)
                .fromGroupId(toUser.getUserWallId())
                .groupName(groupName)
                .createdTime(now)
                .build();
        logger.info("[createNotificationConnect] Finish create notification of connection: " + connectionDTO);
        return notificationService.createNotificationItem(notiItem);
    }

    @Override
    public boolean checkAConnectB(String userAId, String userBId) {
        Connection checkConnectAtoB = connectionRepository.findByUserIdAndUserIdConnected(userAId, userBId);
        return checkConnectAtoB != null && checkConnectAtoB.isConnectStatus();
    }
}
