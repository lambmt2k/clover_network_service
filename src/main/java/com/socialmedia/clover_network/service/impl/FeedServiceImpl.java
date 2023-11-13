package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.CommonConstant;
import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.req.RoleGroupSettingReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.PostItem;
import com.socialmedia.clover_network.mapper.PostMapper;
import com.socialmedia.clover_network.repository.ConnectionRepository;
import com.socialmedia.clover_network.repository.FeedRepository;
import com.socialmedia.clover_network.repository.GroupRepository;
import com.socialmedia.clover_network.service.FeedService;
import com.socialmedia.clover_network.service.GroupService;
import com.socialmedia.clover_network.service.UserService;
import com.socialmedia.clover_network.service.UserWallService;
import com.socialmedia.clover_network.util.GenIDUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class FeedServiceImpl implements FeedService {

    private final Logger logger = LoggerFactory.getLogger(FeedServiceImpl.class);

    @Value("${clover.server_host}")
    private String serverHost;

    private final GroupRepository groupRepository;
    private final ConnectionRepository connectionRepository;
    private final FeedRepository feedRepository;

    private final UserService userService;
    private final GroupService groupService;
    private final UserWallService userWallService;

    @Autowired
    PostMapper postMapper;

    private final GenIDUtil genIDUtil;

    public FeedServiceImpl(GroupRepository groupRepository,
                           ConnectionRepository connectionRepository,
                           FeedRepository feedRepository,
                           UserService userService,
                           GroupService groupService,
                           UserWallService userWallService,
                           GenIDUtil genIDUtil) {
        this.groupRepository = groupRepository;
        this.connectionRepository = connectionRepository;
        this.feedRepository = feedRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.userWallService = userWallService;
        this.genIDUtil = genIDUtil;
    }

    @Override
    public ApiResponse post(FeedItem feedItem) {
        logger.info("Start api [post]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (currentUserId != null) {
            boolean validatedInput = this.validatedFeed(feedItem);
            if(validatedInput) {
                if (StringUtils.isNotEmpty(feedItem.getPrivacyGroupId())) {
                    boolean canPost = this.checkUserCanPostFeedToGroup(feedItem.getPrivacyGroupId(), currentUserId);
                    boolean isUserWall = userWallService.isUserWall(feedItem.getPrivacyGroupId());
                    Optional<GroupEntity> groupOpt = groupRepository.findByGroupId(feedItem.getPrivacyGroupId());
                    if (groupOpt.isPresent() && !groupOpt.get().isDelFlag()) {
                        feedItem.setAuthorId(currentUserId);
                        GroupEntity groupEntity = groupOpt.get();
                        if (!isUserWall) {//case normal group
                            //check owner or post permission
                            if (currentUserId.equals(groupEntity.getGroupOwnerId()) || canPost) {
                                feedItem.setPostToUserWall(false);
                                feedItem = this.postFeed(feedItem);
                                logger.info("[FeedController] postFeed: " + feedItem + " | userId: " + currentUserId);
                                res.setCode(ErrorCode.Feed.ACTION_SUCCESS.getCode());
                                res.setData(feedItem);
                                res.setMessageEN(ErrorCode.Feed.ACTION_SUCCESS.getMessageEN());
                                res.setMessageVN(ErrorCode.Feed.ACTION_SUCCESS.getMessageVN());
                            } else {
                                res.setCode(ErrorCode.Feed.FORBIDDEN.getCode());
                                res.setData(null);
                                res.setMessageEN(ErrorCode.Feed.FORBIDDEN.getMessageEN());
                                res.setMessageVN(ErrorCode.Feed.FORBIDDEN.getMessageVN());
                            }
                        } else {
                            feedItem.setPostToUserWall(true);
                            //case user's wall
                            if (currentUserId.equals(groupEntity.getGroupOwnerId())) {
                                //current user's wall
                                feedItem.setToUserId(currentUserId);
                            } else {
                                //other user's wall
                                feedItem.setToUserId(groupEntity.getGroupOwnerId());
                            }
                            if (canPost) {
                                feedItem = this.postFeed(feedItem);
                                logger.info("[FeedController] postFeed: " + feedItem + " | userId: " + currentUserId);
                                res.setCode(ErrorCode.Feed.ACTION_SUCCESS.getCode());
                                res.setData(feedItem);
                                res.setMessageEN(ErrorCode.Feed.ACTION_SUCCESS.getMessageEN());
                                res.setMessageVN(ErrorCode.Feed.ACTION_SUCCESS.getMessageVN());
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

    private FeedItem postFeed(FeedItem feedItem) {
        String postId = genIDUtil.genId();
        if (postId.isEmpty()) {
            logger.info("[postFeed] error: " + ErrorCode.Feed.GENERATE_POST_ID_ERROR + " | feedItem: " + feedItem);
        }
        LocalDateTime now = LocalDateTime.now();
        feedItem.setPostId(postId);
        feedItem.setCreatedTime(now);
        feedItem.setUpdatedTime(now);
        feedItem.setLastActive(now);
        feedItem.setDelFlag(false);

        String linkPost = serverHost + "/api/feed/detail?postId=" + feedItem.getPostId();
        feedItem.setDynamicLink(linkPost);

        logger.info("[postFeed] Start post feed: " + feedItem);
        try {
            //step 1: insert feedItem to postgres
            PostItem postItem = postMapper.toEntity(feedItem);
            feedRepository.save(postItem);
        } catch (Exception ex) {
            logger.error("[postFeed] " + ex.getMessage() + " | feedItem: " + feedItem);
            ex.printStackTrace();
        }

        return feedItem;
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
