package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.BaseProfile;
import com.socialmedia.clover_network.dto.GroupItem;
import com.socialmedia.clover_network.dto.req.GroupReq;
import com.socialmedia.clover_network.dto.req.RoleGroupSettingReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.GroupRes;
import com.socialmedia.clover_network.dto.res.MemberGroupResDTO;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.GroupMember;
import com.socialmedia.clover_network.entity.GroupRolePermission;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import com.socialmedia.clover_network.enumuration.ImageType;
import com.socialmedia.clover_network.mapper.GroupEntityMapper;
import com.socialmedia.clover_network.repository.*;
import com.socialmedia.clover_network.service.FeedService;
import com.socialmedia.clover_network.service.FirebaseService;
import com.socialmedia.clover_network.service.GroupService;
import com.socialmedia.clover_network.service.UserService;
import com.socialmedia.clover_network.util.GenIDUtil;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @Autowired
    ConnectionRepository connectionRepository;
    @Autowired
    GroupEntityMapper groupEntityMapper;

    @Lazy
    @Autowired
    UserService userService;
    @Lazy
    @Autowired
    FeedService feedService;
    @Autowired
    FirebaseService firebaseService;

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
                groupEntity.setLastActive(now);
                groupRepository.save(groupEntity);

                //add new member into new group
                GroupMember newGroupMember = GroupMember.builder()
                        .groupId(groupEntity.getGroupId())
                        .userId(groupEntity.getGroupOwnerId())
                        .displayName(existedUserInfo.getFirstname() + CommonRegex.REGEX_SPACE + existedUserInfo.getLastname())
                        .groupRoleId(GroupMemberRole.OWNER)
                        .joinTime(now)
                        .leaveTime(null)
                        .status(GroupMember.GroupMemberStatus.APPROVED)
                        .delFlag(false)
                        .build();
                groupMemberRepository.save(newGroupMember);

                //create config role permission of group
                GroupRolePermission ownerRole = new GroupRolePermission(groupId, GroupMemberRole.OWNER, true, true, true, !newGroupMember.isDelFlag());
                GroupRolePermission adminRole = new GroupRolePermission(groupId, GroupMemberRole.ADMIN, true, true, true, !newGroupMember.isDelFlag());
                GroupRolePermission memberRole = new GroupRolePermission(groupId, GroupMemberRole.MEMBER, true, false, false, !newGroupMember.isDelFlag());
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
            List<String> userWallIds = groupRepository.findByGroupTypeAndGroupIdInAndDelFlagFalse(GroupEntity.GroupType.USER_WALL, groupIds)
                    .stream()
                    .map(GroupEntity::getGroupId)
                    .distinct()
                    .collect(Collectors.toList());
            groupIds.removeAll(userWallIds);
            if (groupIds.size() == 0) {
                res.setCode(ErrorCode.Group.GROUP_NOT_FOUND.getCode());
                res.setData(null);
                res.setMessageEN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageEN());
                res.setMessageVN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageVN());
            } else {
                Map<String, GroupEntity> mapGroups = new ConcurrentHashMap<>(10);
                this.multiGetGroupItem(groupIds, mapGroups);
                List<GroupEntity> listGroups = new ArrayList<>(mapGroups.values());
                List<GroupItem> data = new ArrayList<>();
                List<GroupEntity> groupEntities = listGroups.stream().sorted(Comparator.comparing(GroupEntity::getLastActive).reversed()).collect(Collectors.toList());
                groupEntities.forEach(group -> {
                    GroupItem groupItem = groupEntityMapper.toDTO(group);
                    if (group.getAvatarImgUrl() != null) {
                        String avatarUrl = firebaseService.getImagePublicUrl(group.getAvatarImgUrl());
                        groupItem.setAvatarUrl(avatarUrl);
                    }
                    if (group.getBannerImgUrl() != null) {
                        String bannerUrl = firebaseService.getImagePublicUrl(group.getBannerImgUrl());
                        groupItem.setBannerUrl(bannerUrl);
                    }
                    data.add(groupItem);
                });
                res.setCode(ErrorCode.Group.ACTION_SUCCESS.getCode());
                res.setData(data);
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
    public ApiResponse getGroupInfo(String groupId) {
        logger.info("Start api [getGroupInfo]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (currentUserId != null) {
            GroupEntity groupEntity = groupRepository.findByGroupIdAndDelFlagFalse(groupId);
            if (Objects.isNull(groupEntity)) {
                res.setCode(ErrorCode.Group.GROUP_NOT_FOUND.getCode());
                res.setData(groupId);
                res.setMessageEN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageEN());
                res.setMessageVN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageVN());
                return res;
            }
            GroupItem groupItem = groupEntityMapper.toDTO(groupEntity);
            if (groupEntity.getAvatarImgUrl() != null) {
                String avatarUrl = firebaseService.getImagePublicUrl(groupEntity.getAvatarImgUrl());
                groupItem.setAvatarUrl(avatarUrl);
            }
            if (groupEntity.getBannerImgUrl() != null) {
                String bannerUrl = firebaseService.getImagePublicUrl(groupEntity.getBannerImgUrl());
                groupItem.setBannerUrl(bannerUrl);
            }
            List<GroupMember> groupMembers = groupMemberRepository.findAllByGroupIdAndDelFlagFalseAndStatus(groupEntity.getGroupId(), GroupMember.GroupMemberStatus.APPROVED);
            groupItem.setTotalMember(groupMembers.size());
            RoleGroupSettingReq currentUserRole = this.getMemberRolePermission(currentUserId, groupId, groupEntity.getGroupType().equals(GroupEntity.GroupType.USER_WALL));
            res.setCode(ErrorCode.Group.ACTION_SUCCESS.getCode());
            res.setData(GroupRes.GroupInfo.of(groupItem, currentUserRole));
            res.setMessageEN(ErrorCode.Group.ACTION_SUCCESS.getMessageEN());
            res.setMessageVN(ErrorCode.Group.ACTION_SUCCESS.getMessageVN());
        } else {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
        }
        logger.info("End api [getGroupInfo]");
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
    public ApiResponse disableGroup(String groupId, boolean confirm) {
        ApiResponse res = new ApiResponse();
        logger.info("Start API [disableGroup]");
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        LocalDateTime now = LocalDateTime.now();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        GroupEntity groupEntity = groupRepository.findByGroupIdAndDelFlagFalse(groupId);
        if (groupEntity == null) {
            res.setCode(ErrorCode.Group.GROUP_NOT_FOUND.getCode());
            res.setData(groupId);
            res.setMessageEN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageEN());
            res.setMessageVN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageVN());
            return res;
        }
        if (!currentUserId.equals(groupEntity.getGroupOwnerId())) {
            res.setCode(ErrorCode.Group.NOT_PERMISSION.getCode());
            res.setData(groupId);
            res.setMessageEN(ErrorCode.Group.NOT_PERMISSION.getMessageEN());
            res.setMessageVN(ErrorCode.Group.NOT_PERMISSION.getMessageVN());
            return res;
        }
        groupEntity.setDelFlag(confirm);
        groupEntity.setUpdatedTime(now);
        groupEntity.setLastActive(now);
        GroupEntity result = groupRepository.save(groupEntity);
        GroupRes.GroupInfo groupInfo = new GroupRes.GroupInfo();
        GroupItem groupItem = groupEntityMapper.toDTO(result);
        if (result.getAvatarImgUrl() != null) {
            String avatarUrl = firebaseService.getImagePublicUrl(groupEntity.getAvatarImgUrl());
            groupItem.setAvatarUrl(avatarUrl);
        }
        if (result.getBannerImgUrl() != null) {
            String bannerUrl = firebaseService.getImagePublicUrl(groupEntity.getBannerImgUrl());
            groupItem.setBannerUrl(bannerUrl);
        }
        groupInfo.setGroup(groupItem);
        List<GroupMember> groupMembers = groupMemberRepository.findAllByGroupIdAndDelFlagFalseAndStatus(groupEntity.getGroupId(), GroupMember.GroupMemberStatus.APPROVED);
        groupItem.setTotalMember(groupMembers.size());
        RoleGroupSettingReq currentUserRole = this.getMemberRolePermission(currentUserId, groupEntity.getGroupId(), groupEntity.getGroupType().equals(GroupEntity.GroupType.USER_WALL));
        groupInfo.setCurrentUserRole(currentUserRole);
        res.setCode(ErrorCode.Group.ACTION_SUCCESS.getCode());
        res.setData(groupInfo);
        res.setMessageEN(ErrorCode.Group.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.Group.ACTION_SUCCESS.getMessageVN());
        logger.info("End API [disableGroup]");
        return res;
    }

    @Override
    public ApiResponse changeGroupBanner(String groupId, MultipartFile bannerFile) throws IOException {
        ApiResponse res = new ApiResponse();
        logger.info("Start API [disableGroup]");
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        LocalDateTime now = LocalDateTime.now();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        if (bannerFile.isEmpty() || !firebaseService.isImage(bannerFile)) {
            res.setCode(ErrorCode.User.INVALID_IMAGE_FILE.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.User.INVALID_IMAGE_FILE.getMessageEN());
            res.setMessageVN(ErrorCode.User.INVALID_IMAGE_FILE.getMessageVN());
            return res;
        }
        GroupEntity groupEntity = groupRepository.findByGroupIdAndDelFlagFalse(groupId);
        if (groupEntity == null) {
            res.setCode(ErrorCode.Group.GROUP_NOT_FOUND.getCode());
            res.setData(groupId);
            res.setMessageEN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageEN());
            res.setMessageVN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageVN());
            return res;
        }
        if (!currentUserId.equals(groupEntity.getGroupOwnerId())) {
            res.setCode(ErrorCode.Group.NOT_PERMISSION.getCode());
            res.setData(groupId);
            res.setMessageEN(ErrorCode.Group.NOT_PERMISSION.getMessageEN());
            res.setMessageVN(ErrorCode.Group.NOT_PERMISSION.getMessageVN());
            return res;
        }
        String imageFbUrl = firebaseService.uploadImage(bannerFile, ImageType.GROUP_BANNER);
        groupEntity.setBannerImgUrl(imageFbUrl);
        groupEntity.setUpdatedTime(now);
        groupEntity.setLastActive(now);
        GroupEntity result = groupRepository.save(groupEntity);
        GroupRes.GroupInfo groupInfo = new GroupRes.GroupInfo();
        GroupItem groupItem = groupEntityMapper.toDTO(result);
        if (result.getAvatarImgUrl() != null) {
            String avatarUrl = firebaseService.getImagePublicUrl(groupEntity.getAvatarImgUrl());
            groupItem.setAvatarUrl(avatarUrl);
        }
        if (result.getBannerImgUrl() != null) {
            String bannerUrl = firebaseService.getImagePublicUrl(groupEntity.getBannerImgUrl());
            groupItem.setBannerUrl(bannerUrl);
        }
        groupInfo.setGroup(groupItem);
        List<GroupMember> groupMembers = groupMemberRepository.findAllByGroupIdAndDelFlagFalseAndStatus(groupEntity.getGroupId(), GroupMember.GroupMemberStatus.APPROVED);
        groupItem.setTotalMember(groupMembers.size());
        RoleGroupSettingReq currentUserRole = this.getMemberRolePermission(currentUserId, groupEntity.getGroupId(), groupEntity.getGroupType().equals(GroupEntity.GroupType.USER_WALL));
        groupInfo.setCurrentUserRole(currentUserRole);
        res.setCode(ErrorCode.Group.ACTION_SUCCESS.getCode());
        res.setData(groupInfo);
        res.setMessageEN(ErrorCode.Group.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.Group.ACTION_SUCCESS.getMessageVN());
        logger.info("End API [disableGroup]");
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
                    List<GroupMember> allAdminGroup = groupMemberRepository.getActiveRoleOfGroupByGroupId(groupId, GroupMemberRole.ADMIN.getRole());
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
                        memberGroupResDTO.setTotal(allAdminGroup.size());
                        memberGroupResDTO.setMembers(admins);
                    }
                    break;
                }
                case MEMBER: {
                    List<GroupMember> allMemberGroup = groupMemberRepository.getActiveRoleOfGroupByGroupId(groupId, GroupMemberRole.MEMBER.getRole());
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
                        memberGroupResDTO.setTotal(allMemberGroup.size());
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
                Optional<GroupEntity> groupOpt = groupRepository.findByGroupOwnerIdAndGroupType(userId, GroupEntity.GroupType.USER_WALL);
                String userWallId = groupOpt.map(GroupEntity::getGroupId).orElse(null);
                res.put(userId, userWallId);
            });
        }
        return res;
    }
    @Override
    public void addMemberList(String groupId, String userId, boolean isInvited) {
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
                    .displayName(memberInfo != null ? (memberInfo.getFirstname() + CommonRegex.REGEX_SPACE + memberInfo.getLastname()) : null)
                    .groupRoleId(GroupMemberRole.MEMBER)
                    .joinTime(now)
                    .leaveTime(null)
                    .status(GroupMember.GroupMemberStatus.APPROVED)
                    .delFlag(false)
                    .build();
            groupMemberRepository.save(newMember);
        }
    }
    @Override
    public void addWaitingMemberList(String groupId, String userId) {
        logger.info("[addWaitingList] Start add waiting group: " + groupId + "/Member:" + userId);
        LocalDateTime now = LocalDateTime.now();
        Optional<GroupMember> groupMemberOpt = groupMemberRepository.findByUserIdAndGroupId(userId, groupId);
        if (groupMemberOpt.isPresent() && groupMemberOpt.get().isDelFlag()) {
            groupMemberOpt.get().setDelFlag(false);
            groupMemberOpt.get().setJoinTime(now);
            groupMemberOpt.get().setStatus(GroupMember.GroupMemberStatus.WAITING_FOR_APPROVE);
            groupMemberRepository.save(groupMemberOpt.get());
        } else if (groupMemberOpt.isEmpty()) {
            //add new member into new group
            UserInfo memberInfo = userService.getUserInfo(userId);
            GroupMember newMember = GroupMember.builder()
                    .groupId(groupId)
                    .userId(userId)
                    .displayName(memberInfo != null ? (memberInfo.getFirstname() + CommonRegex.REGEX_SPACE + memberInfo.getLastname()) : null)
                    .groupRoleId(GroupMemberRole.MEMBER)
                    .joinTime(now)
                    .leaveTime(null)
                    .status(GroupMember.GroupMemberStatus.WAITING_FOR_APPROVE)
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

    @Override
    public RoleGroupSettingReq getMemberRolePermission(String userId, String groupId, boolean isUserWall) {
        Optional<GroupMember> groupMemberOpt = groupMemberRepository.findFirstByUserIdAndGroupIdAndDelFlagFalse(userId, groupId);
        if (groupMemberOpt.isPresent()) {
            RoleGroupSettingReq roleGroupSettingReq = new RoleGroupSettingReq();
            roleGroupSettingReq.setRoleId(groupMemberOpt.get().getGroupRoleId());
            roleGroupSettingReq.setStatus(groupMemberOpt.get().getStatus());

            Optional<GroupRolePermission> groupRolePermissionOpt = groupRolePermissionRepository.findByGroupIdAndGroupRoleId(groupId, roleGroupSettingReq.getRoleId());
            if (groupRolePermissionOpt.isPresent()) {
                roleGroupSettingReq.setEnablePost(groupRolePermissionOpt.get().isEnablePost());
                roleGroupSettingReq.setEnableComment(groupRolePermissionOpt.get().isEnableComment());
                roleGroupSettingReq.setEnableShare(groupRolePermissionOpt.get().isEnableShare());
            }

            if (isUserWall) {
                Optional<GroupEntity> groupEntityOpt = groupRepository.findByGroupId(groupId);
                if (groupEntityOpt.isPresent()) {
                    String createdUserId = groupEntityOpt.get().getGroupOwnerId();
                    if (userId.equals(createdUserId)) {
                        roleGroupSettingReq.setEnablePost(true);
                    }
                    //case user post to another user's wall
                    roleGroupSettingReq.setEnablePost(connectionRepository.findByUserIdAndUserIdConnectedAndConnectStatusTrue(userId, createdUserId).isPresent());
                }
            }
            return roleGroupSettingReq;
        } else {
            return null;
        }
    }
}
