package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.CommonMessage;
import com.socialmedia.clover_network.dto.req.GroupReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.GroupMember;
import com.socialmedia.clover_network.entity.GroupRolePermission;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import com.socialmedia.clover_network.repository.GroupMemberRepository;
import com.socialmedia.clover_network.repository.GroupRepository;
import com.socialmedia.clover_network.repository.GroupRolePermissionRepository;
import com.socialmedia.clover_network.repository.UserInfoRepository;
import com.socialmedia.clover_network.service.GroupService;
import com.socialmedia.clover_network.util.GenIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {
    private final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    private final GenIDUtil genIDUtil;

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRolePermissionRepository groupRolePermissionRepository;
    private final UserInfoRepository userInfoRepository;

    public GroupServiceImpl(GenIDUtil genIDUtil,
                            GroupRepository groupRepository,
                            GroupMemberRepository groupMemberRepository,
                            GroupRolePermissionRepository groupRolePermissionRepository,
                            UserInfoRepository userInfoRepository) {
        this.genIDUtil = genIDUtil;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupRolePermissionRepository = groupRolePermissionRepository;
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public ApiResponse createNewGroup(GroupReq groupReq) {
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (currentUserId != null) {
            Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(currentUserId);
            if (userInfoOpt.isPresent()) {
                UserInfo existedUserInfo = userInfoOpt.get();
                LocalDateTime now = LocalDateTime.now();
                String groupId = genIDUtil.genId();
                GroupEntity groupEntity = new GroupEntity();
                groupEntity.setGroupId(groupId);
                groupEntity.setGroupName(groupReq.getGroupName());
                groupEntity.setGroupDesc(groupReq.getDescription());
                groupEntity.setGroupOwnerId(currentUserId);
                groupEntity.setGroupType(GroupEntity.GroupType.DEFAULT);
                groupEntity.setGroupPrivacy(groupReq.getGroupPrivacy());
                groupEntity.setEnableComment(true);
                groupEntity.setEnablePost(true);
                groupEntity.setEnableReaction(true);
                groupEntity.setCreatedTime(now);
                groupEntity.setUpdatedTime(now);
                groupRepository.save(groupEntity);

                //add new member into new group
                GroupMember newGroupMember = GroupMember.builder()
                        .groupId(groupEntity.getGroupId())
                        .userId(groupEntity.getGroupOwnerId())
                        .displayName(existedUserInfo.getDisplayName())
                        .groupRoleId(GroupMemberRole.OWNER)
                        .joinTime(now)
                        .leaveTime(null)
                        .status(GroupMember.GroupMemberStatus.APPROVED)
                        .delFlag(false)
                        .build();
                groupMemberRepository.save(newGroupMember);

                //create config role permission of group
                GroupRolePermission ownerRole = new GroupRolePermission(groupId, GroupRolePermission.GroupRole.OWNER, true, true, true, !newGroupMember.isDelFlag());
                GroupRolePermission adminRole = new GroupRolePermission(groupId, GroupRolePermission.GroupRole.ADMIN, true, true, true, !newGroupMember.isDelFlag());
                GroupRolePermission memberRole = new GroupRolePermission(groupId, GroupRolePermission.GroupRole.MEMBER, true, false, false, !newGroupMember.isDelFlag());
                groupRolePermissionRepository.saveAll(Arrays.asList(ownerRole, adminRole, memberRole));

                res.setStatus(HttpStatus.OK.value());
                res.setMessage(CommonMessage.ResponseMessage.ACTION_SUCCESS);
                res.setCode(HttpStatus.OK.toString());
                res.setData(groupEntity);

            } else {
                res.setStatus(HttpStatus.NOT_FOUND.value());
                res.setMessage(CommonMessage.ResponseMessage.ENTITY_NOT_FOUND);
                res.setCode(HttpStatus.NOT_FOUND.toString());
                res.setData(currentUserId);
            }
        } else {
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            res.setMessage(HttpStatus.BAD_REQUEST.getReasonPhrase());
            res.setCode(HttpStatus.BAD_REQUEST.toString());
            res.setData(null);
        }
        return res;
    }

    @Override
    public ApiResponse getListAllGroupOfUser() {
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (currentUserId != null) {
            List<String> groupIds = groupMemberRepository.findByUserIdAndDelFlagFalse(currentUserId)
                    .stream()
                    .map(GroupMember::getGroupId)
                    .distinct()
                    .collect(Collectors.toList());
            List<String> userWallIds = groupRepository.findByGroupTypeAndGroupIdIn(GroupEntity.GroupType.USER_WALL, groupIds)
                    .stream()
                    .map(GroupEntity::getGroupId)
                    .distinct()
                    .collect(Collectors.toList());

            groupIds.removeAll(userWallIds);
            if (groupIds.size() == 0) {
                res.setStatus(HttpStatus.OK.value());
                res.setCode(String.valueOf(HttpStatus.OK.value()));
                res.setMessage(CommonMessage.ResponseMessage.NO_DATA);
                res.setData(null);
            } else {
                Map<String, GroupEntity> mapGroups = new ConcurrentHashMap<>(10);
                this.multiGetGroupItem(groupIds, mapGroups);
                List<GroupEntity> listGroups = new ArrayList<>(mapGroups.values());

                res.setStatus(HttpStatus.OK.value());
                res.setMessage(CommonMessage.ResponseMessage.ACTION_SUCCESS);
                res.setCode(HttpStatus.OK.toString());
                res.setData(listGroups);
            }
        } else {
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            res.setMessage(HttpStatus.BAD_REQUEST.getReasonPhrase());
            res.setCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            res.setData(null);
        }
        return res;
    }

    @Override
    public ApiResponse joinInGroup(String groupId, String userId) {
        return null;
    }

    private void multiGetGroupItem(List<String> groupIds, Map<String, GroupEntity> mapGroupItems) {
        logger.info("[multiGetGroupItem] Start get multi group item for list groupIds: " + groupIds);
        groupIds.forEach(groupId -> {
            Optional<GroupEntity> groupEntityOpt = groupRepository.findByGroupId(groupId);
            groupEntityOpt.ifPresent(groupEntity -> mapGroupItems.put(groupId, groupEntity));
        });
    }
}
