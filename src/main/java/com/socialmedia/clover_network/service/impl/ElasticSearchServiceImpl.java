package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.GroupItem;
import com.socialmedia.clover_network.dto.req.RoleGroupSettingReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.GroupRes;
import com.socialmedia.clover_network.dto.res.SearchRes;
import com.socialmedia.clover_network.dto.res.UserInfoRes;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.GroupMember;
import com.socialmedia.clover_network.mapper.GroupEntityMapper;
import com.socialmedia.clover_network.mapper.PostItemMapper;
import com.socialmedia.clover_network.mapper.UserInfoMapper;
import com.socialmedia.clover_network.repository.FeedRepository;
import com.socialmedia.clover_network.repository.GroupMemberRepository;
import com.socialmedia.clover_network.repository.GroupRepository;
import com.socialmedia.clover_network.repository.UserInfoRepository;
import com.socialmedia.clover_network.service.ConnectionService;
import com.socialmedia.clover_network.service.ElasticSearchService;
import com.socialmedia.clover_network.service.FirebaseService;
import com.socialmedia.clover_network.service.GroupService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    private final Logger logger = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);
    private final UserInfoRepository userInfoRepository;
    private final GroupRepository groupRepository;
    private final FeedRepository feedRepository;

    @Autowired
    GroupService groupService;
    @Autowired
    ConnectionService connectionService;
    @Autowired
    GroupEntityMapper groupEntityMapper;
    @Autowired
    FirebaseService firebaseService;
    @Autowired
    GroupMemberRepository groupMemberRepository;

    public ElasticSearchServiceImpl(UserInfoRepository userInfoRepository,
                                    GroupRepository groupRepository,
                                    FeedRepository feedRepository) {
        this.userInfoRepository = userInfoRepository;
        this.groupRepository = groupRepository;
        this.feedRepository = feedRepository;
    }

    @Override
    public ApiResponse search(String keyword) {
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }

        SearchRes data = new SearchRes();
        List<UserInfoRes> userInfoRes = UserInfoMapper.INSTANCE.toDTOS(userInfoRepository.findByFirstNameOrLastNameContainingIgnoreCase(keyword));
        userInfoRes.removeIf(userInfo -> userInfo.getUserId().equals(currentUserId));
        userInfoRes.forEach(userInfo -> {
            userInfo.setConnected(connectionService.checkAConnectB(currentUserId, userInfo.getUserId()));
        });
        if (!userInfoRes.isEmpty()) {
            data.setUsers((userInfoRes));
        }
        List<GroupEntity> groupEntities = groupRepository.findByDelFlagFalseAndGroupNameContainingIgnoreCase(keyword);
        groupEntities.removeIf(groupEntity -> groupEntity.getGroupType().equals(GroupEntity.GroupType.USER_WALL));
        if (!groupEntities.isEmpty()) {
            List<GroupRes.GroupInfo> groups = new ArrayList<>();
            groupEntities.forEach(groupEntity -> {
                GroupRes.GroupInfo group = new GroupRes.GroupInfo();
                GroupItem groupItem = groupEntityMapper.toDTO(groupEntity);
                if (groupEntity.getAvatarImgUrl() != null) {
                    String avatarUrl = firebaseService.getImagePublicUrl(groupEntity.getAvatarImgUrl());
                    groupItem.setAvatarUrl(avatarUrl);
                }
                if (groupEntity.getBannerImgUrl() != null) {
                    String bannerUrl = firebaseService.getImagePublicUrl(groupEntity.getBannerImgUrl());
                    groupItem.setBannerUrl(bannerUrl);
                }
                group.setGroup(groupItem);
                List<GroupMember> groupMembers = groupMemberRepository.findAllByGroupIdAndDelFlagFalseAndStatus(groupEntity.getGroupId(), GroupMember.GroupMemberStatus.APPROVED);
                groupItem.setTotalMember(groupMembers.size());
                RoleGroupSettingReq currentUserRole = groupService.getMemberRolePermission(currentUserId, groupEntity.getGroupId(), groupEntity.getGroupType().equals(GroupEntity.GroupType.USER_WALL));
                group.setCurrentUserRole(currentUserRole);
                groups.add(group);
            });
            data.setGroups(groups);
        }
        List<FeedItem> feedItems = PostItemMapper.INSTANCE.toDTOS(feedRepository.findByDelFlagFalseAndContentContainingIgnoreCase(keyword));
        if (!feedItems.isEmpty()) {
            data.setFeeds(feedItems);
        }
        res.setCode(ErrorCode.Feed.ACTION_SUCCESS.getCode());
        res.setData(data);
        res.setMessageEN(ErrorCode.Feed.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.Feed.ACTION_SUCCESS.getMessageVN());
        return res;
    }
}
