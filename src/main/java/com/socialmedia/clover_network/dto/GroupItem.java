package com.socialmedia.clover_network.dto;

import com.google.gson.Gson;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.ReactionItem;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import com.socialmedia.clover_network.enumuration.PrivacyPostType;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GroupItem {
    // gson
    private static Gson gson = new Gson();
    @Id
    private Long id;
    private String groupId;
    private String groupName;
    private String avatarUrl;
    private String bannerUrl;
    private String groupDesc;
    private String groupOwnerId;
    private GroupEntity.GroupType groupType;
    private GroupEntity.GroupPrivacy groupPrivacy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private boolean delFlag;


    public String toJson() {
        return gson.toJson(this);
    }
}
