package com.socialmedia.clover_network.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Data
@Builder
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long notificationId;

    @Column(name = "template_id")
    private TemplateNotification templateId;

    @Column(name = "from_user_id")
    private String fromUserId;

    @Column(name = "to_user_id")
    private String toUserId;

    @Column(name = "username")
    private String username;

    /**
     * social: feedId, commentId, groupId
     **/
    @Column(name = "object_id")
    private String objectId;

    @Column(name = "message")
    private String message;

    @Column(name = "from_group_id")
    private String fromGroupId;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "total_react")
    private long totalReact;

    @Column(name = "total_comment")
    private long totalComment;


    public enum TemplateNotification {
        DEFAULT,
        FEED_POST,
        COMMENT,
        LIKE,
        CONNECTION,
        CHANGE_ROLE_GROUP,
        MENTION,
        ANNIVERSARY,
        REQUEST_JOIN_GROUP,
        APPROVE_MEMBER_GROUP
    }
}
