package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.CommonConstant;
import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.BaseProfile;
import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.req.RoleGroupSettingReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.ListFeedRes;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.GroupMember;
import com.socialmedia.clover_network.entity.PostItem;
import com.socialmedia.clover_network.mapper.PostItemMapper;
import com.socialmedia.clover_network.repository.ConnectionRepository;
import com.socialmedia.clover_network.repository.FeedRepository;
import com.socialmedia.clover_network.repository.GroupMemberRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FeedServiceImpl implements FeedService {

    private final Logger logger = LoggerFactory.getLogger(FeedServiceImpl.class);

    @Value("${clover.server_host}")
    private String serverHost;

    private final GroupRepository groupRepository;
    private final ConnectionRepository connectionRepository;
    private final FeedRepository feedRepository;
    @Autowired
    GroupMemberRepository groupMemberRepository;

    /*@Autowired
    FeedItemRepositoryRedis feedItemRedis;
    @Autowired
    FeedUserRepositoryRedis feedUserRedis;*/

    private final UserService userService;
    private final GroupService groupService;
    private final UserWallService userWallService;

    @Autowired
    PostItemMapper postItemMapper;

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
    public ApiResponse post(FeedItem feedItem, List<MultipartFile> files) {
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
                                feedItem = this.postFeed(feedItem, files);
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
                                feedItem = this.postFeed(feedItem, files);
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

    @Override
    public ApiResponse listFeed(int limit, int offset) {
        logger.info("Start API [listFeed]");
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
            Map<String, GroupEntity> mapGroups = new ConcurrentHashMap<>(10);

            this.multiGetGroupItem(groupIds, mapGroups);
            List<GroupEntity> listGroups = new ArrayList<>(mapGroups.values());
            Pageable pageable = PageRequest.of(offset, limit);
            List<PostItem> postItems = feedRepository.findAllByPrivacyGroupIdInAndDelFlagFalseAndLastActiveIsNotNullOrderByLastActiveDesc(groupIds, pageable)
                    .stream()
                    .sorted(Comparator.comparing(PostItem::getLastActive).reversed())
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(postItems)) {
                res.setCode(ErrorCode.Feed.EMPTY_FEED.getCode());
                res.setData(null);
                res.setMessageEN(ErrorCode.Feed.EMPTY_FEED.getMessageEN());
                res.setMessageVN(ErrorCode.Feed.EMPTY_FEED.getMessageVN());
                return res;
            } else {
                List<FeedItem> feedItems = new ArrayList<>();
                for (PostItem postItem: postItems) {
                    FeedItem feedItem = postItemMapper.toDTO(postItem);
                    feedItems.add(feedItem);
                }
                Map<String, FeedItem> mapFeedItem = feedItems.stream().collect(Collectors.toMap(FeedItem::getPostId, feedItem -> feedItem));
                Map<String, RoleGroupSettingReq> mapCurrentUserRole = new LinkedHashMap<>();
                Map<String, GroupEntity> mapGroupItem = new LinkedHashMap<>();

                for (Map.Entry<String, FeedItem> feedItemEntry : mapFeedItem.entrySet()) {
                    FeedItem feedItem = feedItemEntry.getValue();

                    //check is userwall
                    if (null != feedItem.getPrivacyGroupId()) {
                        feedItem.setPostToUserWall(userWallService.isUserWall(feedItem.getPrivacyGroupId()));
                    }
                    //check role of author in group
                    feedItem.setAuthorRoleGroup(
                            groupService.getMemberRolePermission(
                                    feedItem.getAuthorId(),
                                    feedItem.getPrivacyGroupId(),
                                    feedItem.isPostToUserWall()
                            ).getRoleId()
                    );

                    //check role of current user in group
                    RoleGroupSettingReq currentUserRole = groupService.getMemberRolePermission(currentUserId, feedItem.getPrivacyGroupId(), feedItem.isPostToUserWall());
                    mapCurrentUserRole.put(feedItem.getPostId(), currentUserRole);

                    //get info group
                    mapGroupItem.put(feedItem.getPostId(), mapGroups.get(feedItem.getPrivacyGroupId()));
                }
                List<String> feedIds = new ArrayList<>(mapFeedItem.keySet());
                //get userIds
                List<String> listUserIds = new ArrayList<>();
                this.extractMetadata(feedIds, mapFeedItem, listUserIds);

                // get userId => profile
                Map<String, BaseProfile> mapBaseProfile = userService.multiGetBaseProfileByUserIds(listUserIds);
                res.setCode(ErrorCode.Feed.ACTION_SUCCESS.getCode());
                res.setData(ListFeedRes.of(feedIds, mapFeedItem, null, mapBaseProfile, mapGroupItem, mapCurrentUserRole, false));
                res.setMessageEN(ErrorCode.Feed.ACTION_SUCCESS.getMessageEN());
                res.setMessageVN(ErrorCode.Feed.ACTION_SUCCESS.getMessageVN());
                return res;
            }
        } else {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
        }
        logger.info("End API [listFeed]");
        return res;
    }

    private void extractMetadata(List<String> feedIds, Map<String, FeedItem> mapFeedItem, List<String> listUserIds) {
        for (String feedId : feedIds) {
            FeedItem feedItem = mapFeedItem.get(feedId);
            if (feedItem.getAuthorId() != null) {
                listUserIds.add(feedItem.getAuthorId());
            }
            if (feedItem.getToUserId() != null) {
                listUserIds.add(feedItem.getToUserId());
            }
        }
    }

    private void multiGetGroupItem(List<String> groupIds, Map<String, GroupEntity> mapGroupItems) {
        logger.info("[multiGetGroupItem] Start get multi group item for list groupIds: " + groupIds);
        groupIds.forEach(groupId -> {
            Optional<GroupEntity> groupEntityOpt = groupRepository.findByGroupId(groupId);
            groupEntityOpt.ifPresent(groupEntity -> mapGroupItems.put(groupId, groupEntity));
        });
    }

    private FeedItem postFeed(FeedItem feedItem, List<MultipartFile> files) {
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
            PostItem postItem = postItemMapper.toEntity(feedItem);
            feedRepository.save(postItem);
            //step 2: upload file to firebase

            //step 3: insert feed home & group for owner cache and db
            this.insertPostForFeedUser(feedItem.getAuthorId(), feedItem.getPostId());
            this.insertPostForFeedGroup(feedItem);
            //step 4: set last active for group cache

            //step 5: insert post for user in group & notification
            this.broadcastGroup(feedItem, true);

        } catch (Exception ex) {
            logger.error("[postFeed] " + ex.getMessage() + " | feedItem: " + feedItem);
            ex.printStackTrace();
        }

        return feedItem;
    }

    private void insertPostForFeedUser(String userId, String feedId) {

    }

    private void insertPostForFeedGroup(FeedItem feedItem) {

    }

    public void broadcastGroup(FeedItem feedItem, boolean isNotification) {

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
