package com.socialmedia.clover_network.entity;

import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "group_member")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "group_role_id")
    private GroupMemberRole groupRoleId;

    @Column(name = "join_time")
    private LocalDateTime joinTime;

    @Column(name = "leave_time")
    private LocalDateTime leaveTime;

    @Column(name = "status")
    private GroupMemberStatus status;

    @Column(name = "del_flag")
    private boolean delFlag;

    public enum GroupMemberStatus {
        WAITTING_FOR_APPROVE,
        APPROVED;
    }
}
