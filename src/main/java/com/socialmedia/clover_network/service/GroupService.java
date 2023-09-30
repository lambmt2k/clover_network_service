package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.req.GroupReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;

import java.util.List;
import java.util.Map;

public interface GroupService {
    ApiResponse createNewGroup(GroupReq groupReq);
    ApiResponse getListAllGroupOfUser();
    ApiResponse joinGroup(String groupId, String userId);
    ApiResponse getListMemberOfGroup(String groupId, GroupMemberRole roleId, int page, int size);
    ApiResponse searchMemberOfGroup(String groupId, GroupMemberRole roleId, int page, int size, String searchKey);
    Map<String, String> getUserWallIdByUserId(List<String> userIds);
}
