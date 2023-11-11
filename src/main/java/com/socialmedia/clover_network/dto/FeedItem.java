package com.socialmedia.clover_network.dto;

import com.socialmedia.clover_network.entity.PostItem;
import com.socialmedia.clover_network.entity.ReactionItem;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import com.socialmedia.clover_network.enumuration.PrivacyPostType;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FeedItem {
    @Id
    private String postId;
    private String authorId;
    private String toUserId;
    private GroupMemberRole authorRoleGroup;
    private String content;
    private String htmlContent;
    private String dynamicLink;
    private String privacyGroupId;
    private PrivacyPostType privacyType;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private LocalDateTime lastActive;
    private Map<ReactionItem.ReactType, Integer> totalReaction;
    private ReactionItem.ReactType currentUserReact;
    private boolean postToUserWall = false;
    private boolean delFlag = false;
    private boolean isPin = false;
}
