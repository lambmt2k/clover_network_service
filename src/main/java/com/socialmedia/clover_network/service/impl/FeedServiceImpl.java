package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.CommonConstant;
import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.req.RoleGroupSettingReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.repository.ConnectionRepository;
import com.socialmedia.clover_network.repository.GroupRepository;
import com.socialmedia.clover_network.service.FeedService;
import com.socialmedia.clover_network.service.GroupService;
import com.socialmedia.clover_network.service.UserService;
import com.socialmedia.clover_network.service.UserWallService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class FeedServiceImpl implements FeedService {

    private final Logger logger = LoggerFactory.getLogger(FeedServiceImpl.class);

    private final GroupRepository groupRepository;
    private final ConnectionRepository connectionRepository;

    private final UserService userService;
    private final GroupService groupService;
    private final UserWallService userWallService;

    public FeedServiceImpl(GroupRepository groupRepository,
                           ConnectionRepository connectionRepository,
                           UserService userService,
                           GroupService groupService,
                           UserWallService userWallService) {
        this.groupRepository = groupRepository;
        this.connectionRepository = connectionRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.userWallService = userWallService;
    }

    @Override
    public ApiResponse post(FeedItem feedItem) {
        logger.info("Start api [post]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (currentUserId != null) {
            boolean validatedInput = this.validatedFeed(feedItem);
            if(validatedInput) {
                boolean canPost = this.checkUserCanPostFeedToGroup(feedItem.getPrivacyGroupId(), currentUserId);
                boolean isUserWall = userWallService.isUserWall(feedItem.getPrivacyGroupId());
                Optional<GroupEntity> groupOpt = groupRepository.findByGroupId(feedItem.getPrivacyGroupId());
                if (groupOpt.isPresent() && !groupOpt.get().isDelFlag()) {
                    GroupEntity groupEntity = groupOpt.get();
                    if (!isUserWall) {//case normal group
                        //check owner && post permission
                        if (currentUserId.equals(groupEntity.getGroupOwnerId()) && canPost) {

                        } else {
                            res.setCode(ErrorCode.Feed.FORBIDDEN.getCode());
                            res.setData(null);
                            res.setMessageEN(ErrorCode.Feed.FORBIDDEN.getMessageEN());
                            res.setMessageVN(ErrorCode.Feed.FORBIDDEN.getMessageVN());
                        }
                    }
                } else {
                    res.setCode(ErrorCode.Group.GROUP_NOT_FOUND.getCode());
                    res.setData(null);
                    res.setMessageEN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageEN());
                    res.setMessageVN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageVN());
                }

            } else {
                res.setCode(ErrorCode.Feed.INPUT_INVALID.getCode());
                res.setData(null);
                res.setMessageEN(ErrorCode.Feed.INPUT_INVALID.getMessageEN());
                res.setMessageVN(ErrorCode.Feed.INPUT_INVALID.getMessageVN());
            }
        } else {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
        }
        logger.info("End api [post]");
        return res;
    }

    @Override
    public boolean checkUserCanPostFeedToGroup(String groupId, String userId) {
        if (StringUtils.isEmpty(groupId) || StringUtils.isEmpty(userId)) {
            return false;
        }

        Optional<GroupEntity> groupOpt = groupRepository.findByGroupId(groupId);
        if (groupOpt.isPresent() && !groupOpt.get().isDelFlag()) {
            GroupEntity groupEntity = groupOpt.get();
            if (groupEntity.getGroupType().equals(GroupEntity.GroupType.USER_WALL)) {
                String createdUserId = groupEntity.getGroupOwnerId();
                if (userId.equals(createdUserId)) {
                    return true;
                }
                //case user post to another user's wall
                return connectionRepository.findByUserIdAndUserIdConnectedAndConnectStatusTrue(userId, createdUserId).isPresent();
            } else {
                RoleGroupSettingReq roleGroup = groupService.getMemberRolePermission(userId, groupId, false);
                return roleGroup != null && roleGroup.isEnablePost();
            }
        }

        return false;
    }

    private boolean validatedFeed(FeedItem feedItem) {
        if (Objects.isNull(feedItem)) return false;
        String content = feedItem.getContent();
        boolean nullContent = content == null || content.trim().equals(CommonRegex.REGEX_BLANK);
        if (nullContent) return false;
        if (content.length() > CommonConstant.Feed.MAX_CHARACTER_CONTENT_FEED) return false;
        return true;
    }


}
