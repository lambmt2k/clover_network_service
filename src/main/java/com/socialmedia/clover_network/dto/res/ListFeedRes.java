package com.socialmedia.clover_network.dto.res;

import com.socialmedia.clover_network.dto.BaseProfile;
import com.socialmedia.clover_network.dto.CommentDTO;
import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.GroupItem;
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
    private Map<String, BaseProfile> users;
    private Map<String, GroupEntity> groups;
    private Map<String, ReactionItem.ReactType> currentUserReacts;
    private Map<String, Integer> totalReacts;
    private Map<String, Integer> totalComments;
    private Map<String, RoleGroupSettingReq> currentUserRoles;
    private boolean canPost;

    @Getter
    @Setter
    public static class FeedInfoHome {
        private FeedItem feedItem;
        private BaseProfile authorProfile;
        private GroupItem groupItem;
        private RoleGroupSettingReq currentUserRole;
        private Integer totalReact;
        private Integer totalComment;
        private ReactionItem.ReactType currentUserReact;
    }
}
