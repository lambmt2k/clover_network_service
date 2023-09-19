package com.socialmedia.clover_network.entity;

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
@Table(name = "group_role_permission")
public class GroupRolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "group_role_id")
    private GroupRole groupRoleId;

    @Column(name = "enable_comment")
    private boolean enableComment;

    @Column(name = "enable_post")
    private boolean enablePost;

    @Column(name = "enable_share")
    private boolean enableShare;

    @Column(name = "enable_group")
    private boolean enableGroup;

    public enum GroupRole {
        OWNER,
        ADMIN,
        MEMBER;
    }
}
