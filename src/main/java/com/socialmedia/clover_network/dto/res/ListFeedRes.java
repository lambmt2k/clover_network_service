package com.socialmedia.clover_network.dto.res;

import com.socialmedia.clover_network.dto.BaseProfile;
import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.req.RoleGroupSettingReq;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.ReactionItem;
import lombok.*;

import java.util.List;
import java.util.Map;
@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
@Data
public class ListFeedRes {
    private List<String> postIds;
    private Map<String, FeedItem> feeds;
    private Map<String, ReactionItem.ReactType> reactions;
    private Map<String, BaseProfile> users;
    private Map<String, GroupEntity> groups;
    private Map<String, RoleGroupSettingReq> currentUserRoles;
    private boolean canPost;
}