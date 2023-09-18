package com.socialmedia.clover_network.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import com.socialmedia.clover_network.enumuration.PrivacyPostType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "group")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "avatar_img_url")
    private String avatarImgUrl;

    @Column(name = "group_desc")
    private String groupDesc;

    @Column(name = "group_owner_id")
    private String groupOwnerId;

    @Column(name = "group_type")
    private GroupType groupType;

    @Column(name = "group_privacy")
    private GroupPrivacy groupPrivacy;

    @Column(name = "enable_comment")
    private boolean enableComment;

    @Column(name = "enable_post")
    private boolean enablePost;

    @Column(name = "enable_reaction")
    private boolean enableReaction;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "del_flag")
    private boolean delFlag;

    public enum GroupType {
        DEFAULT,
        USER_WALL;

        @JsonValue
        public int toValue() {
            return ordinal();
        }
    }

    public enum GroupPrivacy {
        PUBLIC,
        PRIVATE
    }
}
