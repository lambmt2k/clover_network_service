package com.socialmedia.clover_network.service.impl;

import com.google.gson.Gson;
import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.CommonConstant;
import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.*;
import com.socialmedia.clover_network.dto.req.RoleGroupSettingReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.CheckUserLikeDTO;
import com.socialmedia.clover_network.dto.res.ListFeedRes;
import com.socialmedia.clover_network.entity.*;
import com.socialmedia.clover_network.enumuration.ImageType;
import com.socialmedia.clover_network.mapper.CommentItemMapper;
import com.socialmedia.clover_network.mapper.GroupEntityMapper;
import com.socialmedia.clover_network.mapper.PostItemMapper;
import com.socialmedia.clover_network.repository.*;
import com.socialmedia.clover_network.service.*;
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

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FeedServiceImpl implements FeedService {

    private final Logger logger = LoggerFactory.getLogger(FeedServiceImpl.class);
    private static Gson gson = new Gson();
    @Value("${clover.server_host}")
    private String serverHost;

    private final GroupRepository groupRepository;
    private final ConnectionRepository connectionRepository;
    private final FeedRepository feedRepository;
    @Autowired
    GroupMemberRepository groupMemberRepository;
    @Autowired
    CommentItemRepository commentItemRepository;
    @Autowired
    ReactionItemRepository reactionItemRepository;
    @Autowired
    FeedUserRepository feedUserDAO;
    @Autowired
    FeedGroupRepository feedGroupDAO;
    @Autowired
    GroupEntityMapper groupEntityMapper;

    /*@Autowired
    FeedItemRepositoryRedis feedItemRedis;
    @Autowired
    FeedUserRepositoryRedis feedUserRedis;*/

    private final UserService userService;
    private final GroupService groupService;
    private final UserWallService userWallService;
    @Autowired
    FirebaseService firebaseService;

    @Autowired
    PostItemMapper postItemMapper;
    @Autowired
    CommentItemMapper commentItemMapper;

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
    public ApiResponse post(FeedItem feedItem, List<MultipartFile> images) {
        logger.info("Start api [post]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (currentUserId != null) {
            boolean validatedInput = this.validatedFeed(feedItem);
            if (validatedInput) {
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
                                feedItem = this.postFeed(feedItem, images, groupOpt.get());
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
                                feedItem = this.postFeed(feedItem, images, groupOpt.get());
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
    public ApiResponse listFeedUserHome(int limit, int offset) {
        logger.info("Start API [listFeed]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        logger.info("Get list group of userId {}", currentUserId);
        List<String> groupIds = groupMemberRepository.findByUserIdAndDelFlagFalse(currentUserId)
                .stream()
                .map(GroupMember::getGroupId)
                .distinct()
                .collect(Collectors.toList());
        /*List<String> userWallIds = groupRepository.findByGroupTypeAndGroupIdIn(GroupEntity.GroupType.USER_WALL, groupIds)
                .stream()
                .map(GroupEntity::getGroupId)
                .distinct()
                .collect(Collectors.toList());*/
        Map<String, GroupEntity> mapGroups = new ConcurrentHashMap<>(10);

        this.multiGetGroupItem(groupIds, mapGroups);
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
        }
        List<FeedItem> feedItems = new ArrayList<>();
        for (PostItem postItem : postItems) {
            FeedItem feedItem = postItemMapper.toDTO(postItem);
            if (postItem.getImages().size() > 0) {
                List<String> imageFeeds = new ArrayList<>();
                postItem.getImages().forEach(image -> {
                    imageFeeds.add(firebaseService.getImagePublicUrl(image.getImageUrl()));
                });
                feedItem.setFeedImages(imageFeeds);
            }
            feedItems.add(feedItem);
        }
        Map<String, FeedItem> mapFeedItem = feedItems.stream().collect(Collectors.toMap(FeedItem::getPostId, feedItem -> feedItem));
        Map<String, RoleGroupSettingReq> mapCurrentUserRole = new LinkedHashMap<>();
        Map<String, GroupEntity> mapGroupItem = new LinkedHashMap<>();
        Map<String, Integer> mapTotalComments = new LinkedHashMap<>();
        Map<String, Integer> mapTotalReacts = new LinkedHashMap<>();
        Map<String, ReactionItem.ReactType> mapCurrentUserReacts = new LinkedHashMap<>();

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

            //totalReact
            List<ReactionItem> reactionItems = reactionItemRepository.findByPostIdAndDelFlagFalse(feedItem.getPostId());
            Integer totalReact = reactionItems.size();
            mapTotalReacts.put(feedItem.getPostId(), totalReact);
            //currentUserReact
            ReactionItem currentUserReactionItem = reactionItems.stream().filter(reactionItem -> reactionItem.getAuthorId().equals(currentUserId)).findFirst().orElse(null);
            ReactionItem.ReactType currentUserReactType = currentUserReactionItem != null && !currentUserReactionItem.isDelFlag()
                    ? currentUserReactionItem.getReactType() : null;
            mapCurrentUserReacts.put(feedItem.getPostId(), currentUserReactType);
            //totalComment
            Integer totalComment = commentItemRepository.findByPostIdAndDelFlagFalseOrderByUpdatedTimeDesc(feedItem.getPostId()).size();
            mapTotalComments.put(feedItem.getPostId(), totalComment);

        }
        List<String> feedIds = new ArrayList<>(mapFeedItem.keySet());
        //get userIds
        List<String> listUserIds = new ArrayList<>();
        this.extractMetadata(feedIds, mapFeedItem, listUserIds);

        // get userId => profile
        Map<String, BaseProfile> mapBaseProfile = userService.multiGetBaseProfileByUserIds(listUserIds);
        res.setCode(ErrorCode.Feed.ACTION_SUCCESS.getCode());
        res.setData(ListFeedRes.of(feedIds, mapFeedItem, mapBaseProfile, mapGroupItem, mapCurrentUserReacts, mapTotalReacts, mapTotalComments, mapCurrentUserRole, true));
        res.setMessageEN(ErrorCode.Feed.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.Feed.ACTION_SUCCESS.getMessageVN());
        logger.info("End API [listFeed]");
        return res;

    }

    @Override
    public ApiResponse listFeedUserHomeV2(int page, int size) {
        logger.info("Start API [listFeedUserHomeV2]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        List<String> enableFeedIds = this.getEnableFeedIdOfUser(currentUserId);
        List<ListFeedRes.FeedInfoHome> data = this.listFeed(currentUserId, enableFeedIds, page, size, null);
        if (CollectionUtils.isEmpty(data)) {
            res.setCode(ErrorCode.Feed.EMPTY_FEED.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Feed.EMPTY_FEED.getMessageEN());
            res.setMessageVN(ErrorCode.Feed.EMPTY_FEED.getMessageVN());
            return res;
        }
        res.setCode(ErrorCode.Feed.ACTION_SUCCESS.getCode());
        res.setData(data);
        res.setMessageEN(ErrorCode.Feed.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.Feed.ACTION_SUCCESS.getMessageVN());
        logger.info("End API [listFeedUserHomeV2]");
        return res;

    }

    @Override
    public ApiResponse listFeedGroupHome(String groupId, int page, int size) {
        logger.info("Start API [listFeedGroupHome]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        GroupEntity groupEntity = groupRepository.findByGroupIdAndDelFlagFalse(groupId);
        if (groupEntity.getGroupPrivacy().equals(GroupEntity.GroupPrivacy.PRIVATE)) {
            Optional<GroupMember> groupMember = groupMemberRepository.findByUserIdAndGroupId(currentUserId, groupId);
            if (groupMember.isEmpty() || groupMember.get().getStatus().equals(GroupMember.GroupMemberStatus.WAITING_FOR_APPROVE)) {
                res.setCode(ErrorCode.Feed.NOT_PERMISSION_VIEW_FEED.getCode());
                res.setData(null);
                res.setMessageEN(ErrorCode.Feed.NOT_PERMISSION_VIEW_FEED.getMessageEN());
                res.setMessageVN(ErrorCode.Feed.NOT_PERMISSION_VIEW_FEED.getMessageVN());
                return res;
            }
        }
        List<String> enableFeedGroupIds = this.getEnableFeedIdOfGroup(groupId);
        List<ListFeedRes.FeedInfoHome> data = this.listFeed(currentUserId, enableFeedGroupIds, page, size, groupId);
        if (CollectionUtils.isEmpty(data)) {
            res.setCode(ErrorCode.Feed.EMPTY_FEED.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Feed.EMPTY_FEED.getMessageEN());
            res.setMessageVN(ErrorCode.Feed.EMPTY_FEED.getMessageVN());
            return res;
        }
        res.setCode(ErrorCode.Feed.ACTION_SUCCESS.getCode());
        res.setData(data);
        res.setMessageEN(ErrorCode.Feed.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.Feed.ACTION_SUCCESS.getMessageVN());
        logger.info("End API [listFeedGroupHome]");
        return res;

    }

    @Override
    public ApiResponse listFeedAllGroupHome(int page, int size) {
        logger.info("Start API [listFeedAllGroupHome]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        List<String> enableFeedIds = this.getEnableFeedIdOfUser(currentUserId);
        List<ListFeedRes.FeedInfoHome> data = this.listFeedAllGroup(currentUserId, enableFeedIds, page, size, null);
        if (CollectionUtils.isEmpty(data)) {
            res.setCode(ErrorCode.Feed.EMPTY_FEED.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Feed.EMPTY_FEED.getMessageEN());
            res.setMessageVN(ErrorCode.Feed.EMPTY_FEED.getMessageVN());
            return res;
        }
        res.setCode(ErrorCode.Feed.ACTION_SUCCESS.getCode());
        res.setData(data);
        res.setMessageEN(ErrorCode.Feed.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.Feed.ACTION_SUCCESS.getMessageVN());
        logger.info("End API [listFeedAllGroupHome]");
        return res;

    }

    @Override
    public ApiResponse checkUserLike(String feedId) {
        logger.info("Start API [checkUserLike]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        PostItem postItem = feedRepository.findByPostIdAndDelFlagFalse(feedId);
        if (Objects.isNull(postItem)) {
            res.setCode(ErrorCode.Feed.POST_NOT_FOUND.getCode());
            res.setData(feedId);
            res.setMessageEN(ErrorCode.Feed.POST_NOT_FOUND.getMessageEN());
            res.setMessageVN(ErrorCode.Feed.POST_NOT_FOUND.getMessageVN());
            return res;
        }
        CheckUserLikeDTO data = new CheckUserLikeDTO();
        data.setFeedId(postItem.getPostId());
        //totalReact
        List<ReactionItem> reactionItems = reactionItemRepository.findByPostIdAndDelFlagFalse(postItem.getPostId());
        Integer totalReact = Math.toIntExact(reactionItems.stream().filter(reactionItem -> reactionItem.getReactType().equals(ReactionItem.ReactType.LIKE)).count());
        data.setTotalLike(totalReact);
        //currentUserReact
        ReactionItem currentUserReactionItem = reactionItems.stream().filter(reactionItem -> reactionItem.getAuthorId().equals(currentUserId)).findFirst().orElse(null);
        ReactionItem.ReactType currentUserReactType = currentUserReactionItem != null && !currentUserReactionItem.isDelFlag()
                ? currentUserReactionItem.getReactType() : null;
        if (currentUserReactType != null) {
            data.setCurrentUserLike(currentUserReactType.equals(ReactionItem.ReactType.LIKE));
        }
        res.setCode(ErrorCode.Feed.ACTION_SUCCESS.getCode());
        res.setData(data);
        res.setMessageEN(ErrorCode.Feed.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.Feed.ACTION_SUCCESS.getMessageVN());
        logger.info("End API [checkUserLike]");
        return res;

    }

    public List<ListFeedRes.FeedInfoHome> listFeed(String userId, List<String> feedIds, int page, int size, String groupId) {
        if (CollectionUtils.isEmpty(feedIds)) {
            return null;
        }

        List<ListFeedRes.FeedInfoHome> res = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, size);
        List<PostItem> postItems = feedRepository.findAllByPostIdInAndDelFlagFalseAndLastActiveIsNotNullOrderByLastActiveDesc(feedIds, pageable);
        List<String> groupIds = postItems.stream().map(PostItem::getPrivacyGroupId).distinct().collect(Collectors.toList());
        List<GroupEntity> groupEntities = groupRepository.findAllByGroupIdInAndDelFlagFalse(groupIds);
        postItems.forEach(postItem -> {
            ListFeedRes.FeedInfoHome feedInfoHome = new ListFeedRes.FeedInfoHome();


            //FeedItem
            FeedItem feedItem = PostItemMapper.INSTANCE.toDTO(postItem);
            //groupItem
            GroupEntity groupEntity = groupEntities
                    .stream()
                    .filter(group -> group.getGroupId().equals(feedItem.getPrivacyGroupId()))
                    .findFirst().orElse(null);
            GroupItem groupItem = groupEntityMapper.toDTO(groupEntity);
            feedItem.setPostToUserWall(groupItem.getGroupType().equals(GroupEntity.GroupType.USER_WALL));
            if (postItem.getImages().size() > 0) {
                List<String> imageFeeds = new ArrayList<>();
                postItem.getImages().forEach(image -> {
                    imageFeeds.add(firebaseService.getImagePublicUrl(image.getImageUrl()));
                });
                feedItem.setFeedImages(imageFeeds);
            }
            feedInfoHome.setFeedItem(feedItem);

            //authorProfile
            BaseProfile authorProfile = userService.getBaseProfileByUserId(feedItem.getAuthorId());
            feedInfoHome.setAuthorProfile(authorProfile);



            if (groupEntity != null) {
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
            }
            feedInfoHome.setGroupItem(groupItem);

            //currentUserRole
            RoleGroupSettingReq currentUserRole = groupService.getMemberRolePermission(userId, feedItem.getPrivacyGroupId(), feedItem.isPostToUserWall());
            feedInfoHome.setCurrentUserRole(currentUserRole);

            //totalReact
            List<ReactionItem> reactionItems = reactionItemRepository.findByPostIdAndDelFlagFalse(feedItem.getPostId());
            Integer totalReact = reactionItems.size();
            feedInfoHome.setTotalReact(totalReact);
            //currentUserReact
            ReactionItem currentUserReactionItem = reactionItems.stream().filter(reactionItem -> reactionItem.getAuthorId().equals(userId)).findFirst().orElse(null);
            ReactionItem.ReactType currentUserReactType = currentUserReactionItem != null && !currentUserReactionItem.isDelFlag()
                    ? currentUserReactionItem.getReactType() : null;
            feedInfoHome.setCurrentUserReact(currentUserReactType);
            //totalComment
            Integer totalComment = commentItemRepository.findByPostIdAndDelFlagFalseOrderByUpdatedTimeDesc(feedItem.getPostId()).size();
            feedInfoHome.setTotalComment(totalComment);

            res.add(feedInfoHome);
        });


        return res;
    }

    public List<ListFeedRes.FeedInfoHome> listFeedAllGroup(String userId, List<String> feedIds, int page, int size, String groupId) {
        if (CollectionUtils.isEmpty(feedIds)) {
            return null;
        }

        List<String> allGroupIds = groupMemberRepository.findByUserIdAndDelFlagFalse(userId)
                .stream()
                .map(GroupMember::getGroupId)
                .distinct()
                .collect(Collectors.toList());
        List<String> userWallIds = groupRepository.findByGroupTypeAndGroupIdInAndDelFlagFalse(GroupEntity.GroupType.USER_WALL, allGroupIds)
                .stream()
                .map(GroupEntity::getGroupId)
                .distinct()
                .collect(Collectors.toList());
        allGroupIds.removeAll(userWallIds);

        List<ListFeedRes.FeedInfoHome> res = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, size);
        List<PostItem> postItems = feedRepository.findAllByPostIdInAndPrivacyGroupIdInAndDelFlagFalseAndLastActiveIsNotNullOrderByLastActiveDesc(feedIds, allGroupIds, pageable);
        List<String> groupIds = postItems.stream().map(PostItem::getPrivacyGroupId).distinct().collect(Collectors.toList());
        List<GroupEntity> groupEntities = groupRepository.findAllByGroupIdInAndDelFlagFalse(groupIds);
        postItems.forEach(postItem -> {
            ListFeedRes.FeedInfoHome feedInfoHome = new ListFeedRes.FeedInfoHome();

            //FeedItem
            FeedItem feedItem = PostItemMapper.INSTANCE.toDTO(postItem);
            if (postItem.getImages().size() > 0) {
                List<String> imageFeeds = new ArrayList<>();
                postItem.getImages().forEach(image -> {
                    imageFeeds.add(firebaseService.getImagePublicUrl(image.getImageUrl()));
                });
                feedItem.setFeedImages(imageFeeds);
            }
            feedInfoHome.setFeedItem(feedItem);

            //authorProfile
            BaseProfile authorProfile = userService.getBaseProfileByUserId(feedItem.getAuthorId());
            feedInfoHome.setAuthorProfile(authorProfile);

            //groupItem
            GroupEntity groupEntity = groupEntities
                    .stream()
                    .filter(group -> group.getGroupId().equals(feedItem.getPrivacyGroupId()))
                    .findFirst().orElse(null);
            GroupItem groupItem = groupEntityMapper.toDTO(groupEntity);
            if (groupEntity != null) {
                List<GroupMember> groupMembers = groupMemberRepository.findAllByGroupIdAndDelFlagFalseAndStatus(groupEntity.getGroupId(), GroupMember.GroupMemberStatus.APPROVED);
                groupItem.setTotalMember(groupMembers.size());
                if (groupEntity.getAvatarImgUrl() != null) {
                    String avatarUrl = firebaseService.getImagePublicUrl(groupEntity.getAvatarImgUrl());
                    groupItem.setAvatarUrl(avatarUrl);
                }
                if (groupEntity.getBannerImgUrl() != null) {
                    String bannerUrl = firebaseService.getImagePublicUrl(groupEntity.getBannerImgUrl());
                    groupItem.setBannerUrl(bannerUrl);
                }
            }
            feedInfoHome.setGroupItem(groupItem);

            //currentUserRole
            RoleGroupSettingReq currentUserRole = groupService.getMemberRolePermission(userId, feedItem.getPrivacyGroupId(), feedItem.isPostToUserWall());
            feedInfoHome.setCurrentUserRole(currentUserRole);

            //totalReact
            List<ReactionItem> reactionItems = reactionItemRepository.findByPostIdAndDelFlagFalse(feedItem.getPostId());
            Integer totalReact = reactionItems.size();
            feedInfoHome.setTotalReact(totalReact);
            //currentUserReact
            ReactionItem currentUserReactionItem = reactionItems.stream().filter(reactionItem -> reactionItem.getAuthorId().equals(userId)).findFirst().orElse(null);
            ReactionItem.ReactType currentUserReactType = currentUserReactionItem != null && !currentUserReactionItem.isDelFlag()
                    ? currentUserReactionItem.getReactType() : null;
            feedInfoHome.setCurrentUserReact(currentUserReactType);
            //totalComment
            Integer totalComment = commentItemRepository.findByPostIdAndDelFlagFalseOrderByUpdatedTimeDesc(feedItem.getPostId()).size();
            feedInfoHome.setTotalComment(totalComment);

            res.add(feedInfoHome);
        });


        return res;
    }

    private List<String> getEnableFeedIdOfUser(String userId) {
        List<String> feedIds = new ArrayList<>();
        ;
        FeedUserEntity feedUserEntity = feedUserDAO.findById(userId).orElse(null);
        if (feedUserEntity != null) {
            feedIds = feedUserEntity.getListFeedId();
        }
        return feedIds;
    }

    private List<String> getEnableFeedIdOfGroup(String groupId) {
        List<String> feedGroupIds = new ArrayList<>();
        if (groupId == null || groupId.trim().equals(CommonRegex.REGEX_BLANK)) return feedGroupIds;
        FeedGroupEntity feedGroupEntity = feedGroupDAO.findById(groupId).orElse(null);
        if (feedGroupEntity != null) {
            feedGroupIds = feedGroupEntity.getListFeedId();
        }
        return feedGroupIds;
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

    @Transactional
    private FeedItem postFeed(FeedItem feedItem, List<MultipartFile> images, GroupEntity groupEntity) {
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

        logger.info("[postFeed] Start post feed: " + feedItem);
        try {
            //step 1: insert feedItem to postgres
            PostItem postItem = postItemMapper.toEntity(feedItem);
            if (!CollectionUtils.isEmpty(images)) {
                List<ImageFeedItem> imageFeedItems = new ArrayList<>();
                images.forEach(image -> {
                    if (!image.isEmpty()) {
                        ImageFeedItem newImage = new ImageFeedItem();
                        String imagePath;
                        try {
                            imagePath = firebaseService.uploadImage(image, ImageType.FEED_IMAGES);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        newImage.setImageUrl(imagePath);
                        newImage.setPostItem(postItem);
                        newImage.setCreatedBy(feedItem.getAuthorId());
                        newImage.setCreatedTime(now);
                        newImage.setUpdatedBy(feedItem.getAuthorId());
                        newImage.setUpdatedTime(now);
                        imageFeedItems.add(newImage);
                    }
                });
                if (imageFeedItems.size() > 0) {
                    postItem.setImages(imageFeedItems);
                }
            }
            PostItem successData = feedRepository.save(postItem);
            groupEntity.setLastActive(now);
            groupRepository.save(groupEntity);
            if (Objects.nonNull(successData.getImages()) && successData.getImages().size() > 0) {
                List<String> imageFeeds = new ArrayList<>();
                successData.getImages().forEach(image -> {
                    imageFeeds.add(firebaseService.getImagePublicUrl(image.getImageUrl()));
                });
                feedItem.setFeedImages(imageFeeds);
            }
            //step 2: insert feedItem to redis
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
        FeedUserEntity feedUserEntity = feedUserDAO.findById(userId).orElse(null);
        if (feedUserEntity != null) {
            List<String> feedIds = feedUserEntity.getListFeedId();
            feedIds.add(feedId);
            feedIds = feedIds.stream().distinct().collect(Collectors.toList());
            feedUserEntity.setValue(gson.toJson(feedIds));
            feedUserDAO.save(feedUserEntity);
        } else {
            FeedUserEntity feedUserEntityCreate = FeedUserEntity.builder().key(userId).value(gson.toJson(Collections.singletonList(feedId))).build();
            feedUserDAO.save(feedUserEntityCreate);
        }
    }

    private void insertPostForFeedGroup(FeedItem feedItem) {
        GroupEntity groupEntity = groupRepository.findByGroupIdAndDelFlagFalse(feedItem.getPrivacyGroupId());
        FeedGroupEntity feedGroupEntity = feedGroupDAO.findById(feedItem.getPrivacyGroupId()).orElse(null);
        if (feedGroupEntity != null) {
            List<String> feedIds = feedGroupEntity.getListFeedId();
            feedIds.add(feedItem.getPostId());
            feedIds = feedIds.stream().distinct().collect(Collectors.toList());
            feedGroupEntity.setValue(gson.toJson(feedIds));
            feedGroupDAO.save(feedGroupEntity);
        } else {
            FeedGroupEntity feedGroupEntityCreate = FeedGroupEntity.builder().key(feedItem.getPrivacyGroupId()).value(gson.toJson(Collections.singletonList(feedItem.getPostId()))).build();
            feedGroupDAO.save(feedGroupEntityCreate);
        }

        List<GroupMember> groupMembers = groupMemberRepository.findAllByGroupIdAndDelFlagFalseAndStatus(groupEntity.getGroupId(), GroupMember.GroupMemberStatus.APPROVED);
        if (!CollectionUtils.isEmpty(groupMembers)) {
            groupMembers.forEach(member -> {
                this.insertPostForFeedUser(member.getUserId(), feedItem.getPostId());
            });
        }
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
                boolean AConnectB = connectionRepository.findByUserIdAndUserIdConnectedAndConnectStatusTrue(userId, createdUserId).isPresent();
                boolean BConnectA = connectionRepository.findByUserIdAndUserIdConnectedAndConnectStatusTrue(createdUserId, userId).isPresent();
                return AConnectB && BConnectA;
            } else {
                RoleGroupSettingReq roleGroup = groupService.getMemberRolePermission(userId, groupId, false);
                return roleGroup != null && roleGroup.isEnablePost();
            }
        }

        return false;
    }

    @Override
    public ApiResponse commentToFeed(CommentDTO commentDTO) {
        logger.info("Start API [commentToFeed]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        //check post existed
        PostItem postItem = feedRepository.findByPostIdAndDelFlagFalse(commentDTO.getPostId());
        if (postItem == null) {
            res.setCode(ErrorCode.Feed.EMPTY_FEED.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Feed.EMPTY_FEED.getMessageEN());
            res.setMessageVN(ErrorCode.Feed.EMPTY_FEED.getMessageVN());
            return res;
        }
        //check group existed
        GroupEntity groupEntity = groupRepository.findByGroupIdAndDelFlagFalse(postItem.getPrivacyGroupId());
        if (groupEntity == null) {
            res.setCode(ErrorCode.Group.GROUP_NOT_FOUND.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageEN());
            res.setMessageVN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageVN());
            return res;
        }

        //validate input comment
        boolean validComment = this.validatedComment(commentDTO);
        if (!validComment) {
            res.setCode(ErrorCode.Comment.INPUT_INVALID.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Comment.INPUT_INVALID.getMessageEN());
            res.setMessageVN(ErrorCode.Comment.INPUT_INVALID.getMessageVN());
            return res;
        }

        //check comment permission
        RoleGroupSettingReq currentUserRole = groupService.getMemberRolePermission(
                currentUserId,
                groupEntity.getGroupId(),
                groupEntity.getGroupType().equals(GroupEntity.GroupType.USER_WALL));
        if (currentUserRole == null || !currentUserRole.isEnableComment()) {
            res.setCode(ErrorCode.Group.DISABLE_COMMENT.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Group.DISABLE_COMMENT.getMessageEN());
            res.setMessageVN(ErrorCode.Group.DISABLE_COMMENT.getMessageVN());
            return res;
        }

        CommentDTO.CommentInfo commentRes = this.commentToPost(currentUserId, commentDTO, postItem);

        res.setCode(ErrorCode.Comment.ACTION_SUCCESS.getCode());
        res.setData(commentRes);
        res.setMessageEN(ErrorCode.Comment.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.Comment.ACTION_SUCCESS.getMessageVN());
        logger.info("Finish API [commentToFeed]");
        return res;
    }

    @Override
    public ApiResponse getCommentOfPost(String postId, int page, int size) {
        logger.info("Start API [getCommentOfPost]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        PostItem postItem = feedRepository.findByPostIdAndDelFlagFalse(postId);
        if (Objects.isNull(postItem)) {
            res.setCode(ErrorCode.Feed.POST_NOT_FOUND.getCode());
            res.setData(postId);
            res.setMessageEN(ErrorCode.Feed.POST_NOT_FOUND.getMessageEN());
            res.setMessageVN(ErrorCode.Feed.POST_NOT_FOUND.getMessageVN());
            return res;
        }
        Pageable pageable = PageRequest.of(page, size);
        List<CommentItem> commentItems = commentItemRepository.findByPostIdAndDelFlagFalseOrderByUpdatedTimeDesc(postId,pageable);
        List<CommentDTO.CommentInfo> data = new ArrayList<>();
        commentItems.forEach(commentItem -> {
            CommentDTO.CommentInfo commentInfo = this.convertCommentItemToCommentInfo(commentItem, currentUserId);
            data.add(commentInfo);
        });
        res.setCode(ErrorCode.Comment.ACTION_SUCCESS.getCode());
        res.setData(data);
        res.setMessageEN(ErrorCode.Comment.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.Comment.ACTION_SUCCESS.getMessageVN());
        logger.info("Finish API [getCommentOfPost]");
        return res;
    }

    @Override
    public ApiResponse reactToFeed(ReactDTO reactDTO) {
        logger.info("Start API [reactToFeed]");
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
            return res;
        }
        //check post existed
        PostItem postItem = feedRepository.findByPostIdAndDelFlagFalse(reactDTO.getPostId());
        if (postItem == null) {
            res.setCode(ErrorCode.Feed.EMPTY_FEED.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Feed.EMPTY_FEED.getMessageEN());
            res.setMessageVN(ErrorCode.Feed.EMPTY_FEED.getMessageVN());
            return res;
        }
        //check group existed
        GroupEntity groupEntity = groupRepository.findByGroupIdAndDelFlagFalse(postItem.getPrivacyGroupId());
        if (groupEntity == null) {
            res.setCode(ErrorCode.Group.GROUP_NOT_FOUND.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageEN());
            res.setMessageVN(ErrorCode.Group.GROUP_NOT_FOUND.getMessageVN());
            return res;
        }

        ReactionItem existedReact = reactionItemRepository.findByAuthorIdAndPostIdAndDelFlagFalse(currentUserId, reactDTO.getPostId());
        Long reactionId;
        LocalDateTime now = LocalDateTime.now();
        if (existedReact != null) {
            existedReact.setReactType(reactDTO.getReactType());
            existedReact.setUpdatedTime(now);
            existedReact.setDelFlag(!reactDTO.isStatus());
            reactionItemRepository.save(existedReact);
            reactionId = existedReact.getReactionId();
        } else {
            ReactionItem newReactionItem = new ReactionItem();
            newReactionItem.setAuthorId(currentUserId);
            newReactionItem.setReactType(reactDTO.getReactType());
            newReactionItem.setGroupId(groupEntity.getGroupId());
            newReactionItem.setPostId(postItem.getPostId());
            newReactionItem.setCreatedTime(now);
            newReactionItem.setUpdatedTime(now);
            newReactionItem.setDelFlag(!reactDTO.isStatus());
            reactionId = reactionItemRepository.save(newReactionItem).getReactionId();
        }

        res.setCode(ErrorCode.Reaction.ACTION_SUCCESS.getCode());
        res.setData(reactionId);
        res.setMessageEN(ErrorCode.Reaction.ACTION_SUCCESS.getMessageEN());
        res.setMessageVN(ErrorCode.Reaction.ACTION_SUCCESS.getMessageVN());
        return res;
    }

    private CommentDTO.CommentInfo commentToPost(String authorId, CommentDTO commentDTO, PostItem postItem) {
        commentDTO.setAuthorId(authorId);
        LocalDateTime now = LocalDateTime.now();
        commentDTO.setCreatedTime(now);
        commentDTO.setUpdatedTime(now);
        commentDTO.setDelFlag(false);
        CommentItem commentItem = commentItemMapper.toEntity(commentDTO);
        CommentItem data = commentItemRepository.save(commentItem);

        postItem.setLastActive(now);
        feedRepository.save(postItem);

        return this.convertCommentItemToCommentInfo(data, authorId);
    }

    private CommentDTO.CommentInfo convertCommentItemToCommentInfo(CommentItem commentItem, String currentUserId) {
        CommentDTO.CommentInfo res = new CommentDTO.CommentInfo();
        res.setCommentId(commentItem.getCommentId());
        res.setPostId(commentItem.getPostId());
        Map<String, BaseProfile> authorProfile = userService.multiGetBaseProfileByUserIds(Collections.singletonList(commentItem.getAuthorId()));
        res.setAuthorProfile(authorProfile.get(commentItem.getAuthorId()));
        res.setContent(commentItem.getContent());
        res.setCreatedTime(commentItem.getCreatedTime());
        res.setUpdatedTime(commentItem.getUpdatedTime());
        res.setParentCommentId(commentItem.getParentCommentId());
        res.setLevel(commentItem.getLevel());
        res.setAuthor(currentUserId.equals(commentItem.getAuthorId()));
        return res;
    }

    private boolean validatedComment(CommentDTO commentDTO) {
        if (commentDTO == null) {
            return false;
        }
        if (commentDTO.getContent() == null || commentDTO.getContent().isEmpty()) {
            return false;
        }
        return true;
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
