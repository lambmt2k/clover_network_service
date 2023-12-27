package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.ConnectionDTO;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.UserInfoRes;
import com.socialmedia.clover_network.entity.Connection;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.GroupMember;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.UserStatus;
import com.socialmedia.clover_network.mapper.UserInfoMapper;
import com.socialmedia.clover_network.repository.ConnectionRepository;
import com.socialmedia.clover_network.repository.GroupMemberRepository;
import com.socialmedia.clover_network.service.ConnectionService;
import com.socialmedia.clover_network.service.GroupService;
import com.socialmedia.clover_network.service.UserService;
import com.socialmedia.clover_network.service.UserWallService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class ConnectionServiceImpl implements ConnectionService {

    private final Logger logger = LoggerFactory.getLogger(ConnectionServiceImpl.class);

    @Autowired
    UserService userService;
    @Autowired
    UserWallService userWallService;
    @Autowired
    GroupService groupService;
    @Autowired
    ConnectionRepository connectionRepository;
    @Autowired
    GroupMemberRepository groupMemberRepository;


    @Override
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
                connectionRepository.save(checkConnectAtoB);
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
                connectionRepository.save(newConnection);

                res.setCode(ErrorCode.Connection.ACTION_SUCCESS.getCode());
                res.setData(targetUserInfo.getUserId());
                res.setMessageEN(ErrorCode.Connection.ACTION_SUCCESS.getMessageEN());
                res.setMessageVN(ErrorCode.Connection.ACTION_SUCCESS.getMessageVN());
            }
        }

        return res;
    }

    @Override
    public boolean checkAConnectB(String userAId, String userBId) {
        Connection checkConnectAtoB = connectionRepository.findByUserIdAndUserIdConnected(userAId, userBId);
        return checkConnectAtoB != null && checkConnectAtoB.isConnectStatus();
    }
}
