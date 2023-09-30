package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.BaseProfile;
import com.socialmedia.clover_network.dto.req.GroupReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.MemberGroupResDTO;
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
import com.socialmedia.clover_network.service.UserService;
import com.socialmedia.clover_network.util.GenIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {
    private final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    @Autowired
    GenIDUtil genIDUtil;

    @Autowired
    GroupRepository groupRepository;
    @Autowired
    GroupMemberRepository groupMemberRepository;
    @Autowired
    GroupRolePermissionRepository groupRolePermissionRepository;
    @Autowired
    UserInfoRepository userInfoRepository;

    @Lazy
    @Autowired
    UserService userService;

    @Override
    public ApiResponse createNewGroup(GroupReq groupReq) {
        logger.info("Start api [createNewGroup]");
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

                res.setCode(ErrorCode.Group.ACTION_SUCCESS.getCode());
                res.setData(groupEntity);
                res.setMessageEN(ErrorCode.Group.ACTION_SUCCESS.getMessageEN());
                res.setMessageVN(ErrorCode.Group.ACTION_SUCCESS.getMessageVN());
            } else {
                res.setCode(ErrorCode.User.PROFILE_GET_EMPTY.getCode());
                res.setData(currentUserId);
                res.setMessageEN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageEN());
                res.setMessageVN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageEN());
            }
        } else {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
        }
        logger.info("End api [createNewGroup]");
        return res;
    }

    @Override
    public ApiResponse getListAllGroupOfUser() {
        logger.info("Start api [getListAllGroupOfUser]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (currentUserId != null) {
            logger.info("Get list group of userId {}", currentUserId);
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
            /*groupIds.removeAll(userWallIds);*/
            if (groupIds.size() == 0) {
                res.setCode(ErrorCode.Group.GROUP_NOT_FOUND.getCode());
                res.setData(null);
                res.setMessageEN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageEN());
                res.setMessageVN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageVN());
            } else {
                Map<String, GroupEntity> mapGroups = new ConcurrentHashMap<>(10);
                this.multiGetGroupItem(groupIds, mapGroups);
                List<GroupEntity> listGroups = new ArrayList<>(mapGroups.values());
                res.setCode(ErrorCode.Group.ACTION_SUCCESS.getCode());
                res.setData(listGroups);
                res.setMessageEN(ErrorCode.Group.ACTION_SUCCESS.getMessageEN());
                res.setMessageVN(ErrorCode.Group.ACTION_SUCCESS.getMessageVN());
            }
        } else {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
        }
        logger.info("End api [getListAllGroupOfUser]");
        return res;
    }

    @Override
    public ApiResponse joinGroup(String groupId, String userId) {
        logger.info("Start api [joinGroup]: userId {} join groupId {}", userId, groupId);
        ApiResponse res = new ApiResponse();
        Optional<GroupEntity> groupOpt = groupRepository.findByGroupId(groupId);
        if (groupOpt.isPresent() && !groupOpt.get().isDelFlag()) {
            GroupEntity groupEntity = groupOpt.get();
            Optional<GroupMember> groupMemberOpt = groupMemberRepository.findByUserIdAndGroupId(userId, groupId);
            if (groupMemberOpt.isPresent() && !groupMemberOpt.get().isDelFlag()) {
                res.setCode(ErrorCode.Group.ALREADY_MEMBER.getCode());
                res.setData(groupId);
                res.setMessageEN(ErrorCode.Group.ALREADY_MEMBER.getMessageEN());
                res.setMessageVN(ErrorCode.Group.ALREADY_MEMBER.getMessageVN());
            } else {
                if (groupEntity.getGroupPrivacy().equals(GroupEntity.GroupPrivacy.PUBLIC)) {
                    //add userid to member group
                    this.addMemberList(groupId, userId, false);
                } else if (groupEntity.getGroupPrivacy().equals(GroupEntity.GroupPrivacy.PRIVATE)) {
                    //add userid to list waiting
                    this.addWaitingMemberList(groupId, userId);
                }
                res.setCode(ErrorCode.Group.ACTION_SUCCESS.getCode());
                res.setData(groupId);
                res.setMessageEN(ErrorCode.Group.ACTION_SUCCESS.getMessageEN());
                res.setMessageVN(ErrorCode.Group.ACTION_SUCCESS.getMessageVN());
            }
        } else {
            res.setCode(ErrorCode.Group.GROUP_NOT_FOUND.getCode());
            res.setData(groupId);
            res.setMessageEN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageEN());
            res.setMessageVN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageVN());
        }
        logger.info("End api [joinGroup]: userId {} join groupId {}", userId, groupId);
        return res;
    }

    @Override
    public ApiResponse getListMemberOfGroup(String groupId, GroupMemberRole roleId, int page, int size) {
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (Objects.nonNull(currentUserId)) {
            MemberGroupResDTO memberGroupResDTO = new MemberGroupResDTO();
            Pageable pageable = PageRequest.of(page, size, Sort.by("display_name"));
            switch (roleId) {
                case OWNER: {
                    GroupMember ownerGroup = groupMemberRepository.findByGroupIdAndGroupRoleIdAndDelFlagFalse(groupId, GroupMemberRole.OWNER).orElse(null);
                    if (Objects.nonNull(ownerGroup)) {
                        String ownerId = ownerGroup.getUserId();
                        Map<String, BaseProfile> mapProfile = userService.multiGetBaseProfileByUserIds(Collections.singletonList(ownerGroup.getUserId()));
                        memberGroupResDTO.setTotal(1);
                        memberGroupResDTO.setMembers(Collections.singletonList(mapProfile.get(ownerId)));
                    }
                    break;
                }
                case ADMIN: {
                    Page<GroupMember> adminGroups = groupMemberRepository.getActiveRoleOfGroupByGroupId(groupId, GroupMemberRole.ADMIN.getRole(), pageable);
                    if (adminGroups.getTotalElements() > 0) {
                        List<String> adminUserIds = new ArrayList<>();
                        for (GroupMember member : adminGroups) {
                            adminUserIds.add(member.getUserId());
                        }
                        Map<String, BaseProfile> mapProfile = userService.multiGetBaseProfileByUserIds(adminUserIds);
                        List<BaseProfile> admins = new ArrayList<>();
                        for (String userId : adminUserIds) {
                            BaseProfile bProfile = mapProfile.get(userId);
                            if (bProfile != null) {
                                admins.add(bProfile);
                            }
                        }
                        memberGroupResDTO.setTotal((int) adminGroups.getTotalElements());
                        memberGroupResDTO.setMembers(admins);
                    }
                    break;
                }
                case MEMBER: {
                    Page<GroupMember> memberGroups = groupMemberRepository.getActiveRoleOfGroupByGroupId(groupId, GroupMemberRole.MEMBER.getRole(), pageable);
                    if (memberGroups.getTotalElements() > 0) {
                        List<String> memberUserIds = new ArrayList<>();
                        for (GroupMember member : memberGroups) {
                            memberUserIds.add(member.getUserId());
                        }
                        Map<String, BaseProfile> mapProfile = userService.multiGetBaseProfileByUserIds(memberUserIds);
                        List<BaseProfile> member = new ArrayList<>();
                        for (String userId : memberUserIds) {
                            BaseProfile bProfile = mapProfile.get(userId);
                            if (bProfile != null) {
                                member.add(bProfile);
                            }
                        }
                        memberGroupResDTO.setTotal((int) memberGroups.getTotalElements());
                        memberGroupResDTO.setMembers(member);
                    }
                    break;
                }
                default: {
                    break;
                }
            }

            res.setCode(ErrorCode.Group.ACTION_SUCCESS.getCode());
            res.setData(memberGroupResDTO);
            res.setMessageEN(ErrorCode.Group.ACTION_SUCCESS.getMessageEN());
            res.setMessageVN(ErrorCode.Group.ACTION_SUCCESS.getMessageVN());
        } else {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
        }
        return res;
    }

    @Override
    public ApiResponse searchMemberOfGroup(String groupId, GroupMemberRole roleId, int page, int size, String searchKey) {
        return null;
    }

    @Override
    public Map<String, String> getUserWallIdByUserId(List<String> userIds) {
        Map<String, String> res = new HashMap<>();
        if (!CollectionUtils.isEmpty(userIds)) {
            userIds.forEach(userId -> {
                String userWallId = groupRepository.findByGroupOwnerIdAndGroupType(userId, GroupEntity.GroupType.USER_WALL).getGroupId();
                res.put(userId, userWallId);
            });
        }
        return res;
    }

    private void addMemberList(String groupId, String userId, boolean isInvited) {
        logger.info("[addMemberList] Start add member group: " + groupId + "/Member:" + userId);
        LocalDateTime now = LocalDateTime.now();
        Optional<GroupMember> groupMemberOpt = groupMemberRepository.findByUserIdAndGroupId(userId, groupId);
        if (groupMemberOpt.isPresent() && groupMemberOpt.get().isDelFlag()) {
            groupMemberOpt.get().setDelFlag(false);
            groupMemberOpt.get().setJoinTime(now);
            groupMemberOpt.get().setStatus(GroupMember.GroupMemberStatus.APPROVED);
            groupMemberRepository.save(groupMemberOpt.get());
        } else if (groupMemberOpt.isEmpty()) {
            //add new member into new group
            UserInfo memberInfo = userService.getUserInfo(userId);
            GroupMember newMember = GroupMember.builder()
                    .groupId(groupId)
                    .userId(userId)
                    .displayName(memberInfo != null ? memberInfo.getDisplayName() : null)
                    .groupRoleId(GroupMemberRole.MEMBER)
                    .joinTime(now)
                    .leaveTime(null)
                    .status(GroupMember.GroupMemberStatus.APPROVED)
                    .delFlag(false)
                    .build();
            groupMemberRepository.save(newMember);
        }
    }

    private void addWaitingMemberList(String groupId, String userId) {
        logger.info("[addWaitingList] Start add waiting group: " + groupId + "/Member:" + userId);
        LocalDateTime now = LocalDateTime.now();
        Optional<GroupMember> groupMemberOpt = groupMemberRepository.findByUserIdAndGroupId(userId, groupId);
        if (groupMemberOpt.isPresent() && groupMemberOpt.get().isDelFlag()) {
            groupMemberOpt.get().setDelFlag(false);
            groupMemberOpt.get().setJoinTime(now);
            groupMemberOpt.get().setStatus(GroupMember.GroupMemberStatus.WAITTING_FOR_APPROVE);
            groupMemberRepository.save(groupMemberOpt.get());
        } else if (groupMemberOpt.isEmpty()) {
            //add new member into new group
            UserInfo memberInfo = userService.getUserInfo(userId);
            GroupMember newMember = GroupMember.builder()
                    .groupId(groupId)
                    .userId(userId)
                    .displayName(memberInfo != null ? memberInfo.getDisplayName() : null)
                    .groupRoleId(GroupMemberRole.MEMBER)
                    .joinTime(now)
                    .leaveTime(null)
                    .status(GroupMember.GroupMemberStatus.WAITTING_FOR_APPROVE)
                    .delFlag(false)
                    .build();
            groupMemberRepository.save(newMember);
        }
    }

    private void multiGetGroupItem(List<String> groupIds, Map<String, GroupEntity> mapGroupItems) {
        logger.info("[multiGetGroupItem] Start get multi group item for list groupIds: " + groupIds);
        groupIds.forEach(groupId -> {
            Optional<GroupEntity> groupEntityOpt = groupRepository.findByGroupId(groupId);
            groupEntityOpt.ifPresent(groupEntity -> mapGroupItems.put(groupId, groupEntity));
        });
    }
}
