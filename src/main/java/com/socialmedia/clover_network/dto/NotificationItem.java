package com.socialmedia.clover_network.dto;

import com.google.gson.Gson;
import com.socialmedia.clover_network.entity.NotificationEntity;
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
public class NotificationItem {
    // gson
    private static Gson gson = new Gson();

    @Id
    private Long notificationId;
    private NotificationEntity.TemplateNotification templateId;
    private String fromUserId;
    private String toUserId;
    private String username;
    private String objectId;
    private String message;
    private String fromGroupId;
    private String groupName;
    private LocalDateTime createdTime;
    private long totalReact;
    private long totalComment;


    public String toJson() {
        return gson.toJson(this);
    }
}
