package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.req.GroupReq;
import com.socialmedia.clover_network.dto.req.RoleGroupSettingReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface GroupService {
    ApiResponse createNewGroup(GroupReq groupReq);
    ApiResponse getListAllGroupOfUser();
    ApiResponse getGroupInfo(String groupId);
    ApiResponse joinGroup(String groupId, String userId);
    ApiResponse leaveGroup(String groupId);
    boolean canPost(String groupId);
    ApiResponse disableGroup(String groupId, boolean confirm);
    ApiResponse changeGroupBanner(String groupId, MultipartFile bannerFile) throws IOException;
    ApiResponse getListMemberOfGroup(String groupId, GroupMemberRole roleId, int page, int size);
    ApiResponse searchMemberOfGroup(String groupId, GroupMemberRole roleId, int page, int size, String searchKey);
    Map<String, String> getUserWallIdByUserId(List<String> userIds);
    RoleGroupSettingReq getMemberRolePermission(String userId, String groupId, boolean isUserWall);
    void addMemberList(String groupId, String userId, boolean isInvited);
    void addWaitingMemberList(String groupId, String userId);

}
