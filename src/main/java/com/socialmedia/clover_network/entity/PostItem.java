package com.socialmedia.clover_network.entity;

import com.socialmedia.clover_network.enumuration.Favorite;
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
@Table(name = "post_item")
public class PostItem {
    @Id
    @Column(name = "post_id")
    private String postId;

    @Column(name = "author_id")
    private String authorId;

    @Column(name = "to_user_id")
    private String toUserId;

    @Column(name = "author_role_group")
    private GroupMemberRole authorRoleGroup;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    @Column(name = "dynamic_link")
    private String dynamicLink;

    @Column(name = "privacy_group_id")
    private String privacyGroupId;

    @Column(name = "privacy_type")
    private PrivacyPostType privacyType;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @Column(name = "del_flag")
    private boolean delFlag;
}
