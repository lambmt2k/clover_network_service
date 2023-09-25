package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.GroupMember;
import com.socialmedia.clover_network.entity.GroupRolePermission;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import com.socialmedia.clover_network.enumuration.UserStatus;
import com.socialmedia.clover_network.repository.GroupMemberRepository;
import com.socialmedia.clover_network.repository.GroupRepository;
import com.socialmedia.clover_network.repository.GroupRolePermissionRepository;
import com.socialmedia.clover_network.repository.UserInfoRepository;
import com.socialmedia.clover_network.service.UserWallService;
import com.socialmedia.clover_network.util.GenIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserWallServiceImpl implements UserWallService {

    private final Logger logger = LoggerFactory.getLogger(UserWallServiceImpl.class);

    private final UserInfoRepository userInfoRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRolePermissionRepository groupRolePermissionRepository;

    private final GenIDUtil genIDUtil;

    public UserWallServiceImpl(UserInfoRepository userInfoRepository,
                               GroupRepository groupRepository,
                               GroupMemberRepository groupMemberRepository,
                               GroupRolePermissionRepository groupRolePermissionRepository,
                               GenIDUtil genIDUtil) {
        this.userInfoRepository = userInfoRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupRolePermissionRepository = groupRolePermissionRepository;
        this.genIDUtil = genIDUtil;
    }

    @Override
    public String createUserWall(String userId) {
        logger.info("Start create user's wall API for userId: {}", userId);
        Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(userId);
        if (userInfoOpt.isPresent()) {
            UserInfo existedUserInfo = userInfoOpt.get();
            if (!hasUserWall(userId, existedUserInfo)) {
                LocalDateTime now = LocalDateTime.now();

                //create new group
                String groupId = genIDUtil.genId();
                GroupEntity newUserWall = new GroupEntity();
                newUserWall.setGroupId(groupId);
                newUserWall.setGroupOwnerId(userId);
                newUserWall.setGroupName(existedUserInfo.getDisplayName());
                newUserWall.setGroupDesc(existedUserInfo.getDisplayName() + "'s Wall");
                newUserWall.setGroupType(GroupEntity.GroupType.USER_WALL);
                newUserWall.setGroupPrivacy(GroupEntity.GroupPrivacy.PUBLIC);
                newUserWall.setEnableComment(true);
                newUserWall.setEnablePost(true);
                newUserWall.setEnableReaction(true);
                newUserWall.setCreatedTime(now);
                newUserWall.setUpdatedTime(now);
                groupRepository.save(newUserWall);

                //add new member into new group
                GroupMember newGroupMember = GroupMember.builder()
                        .groupId(newUserWall.getGroupId())
                        .userId(newUserWall.getGroupOwnerId())
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

                return newUserWall.getGroupId();
            }
        }
        return null;
    }

    @Override
    public boolean hasUserWall(String userId, UserInfo existedUserInfo) {
        logger.info("Start check if userId {} had user's wall or not", userId);
        if (userId != null) {
            if (existedUserInfo.getStatus().equals(UserStatus.ACTIVE)) {
                //find group owned by userId
                List<GroupEntity> groupEntities = groupRepository.findByGroupOwnerId(userId);
                return groupEntities.stream().anyMatch(group -> (
                        group.getGroupType().equals(GroupEntity.GroupType.USER_WALL) && !group.isDelFlag()));
            }
        }
        return false;
    }
}
