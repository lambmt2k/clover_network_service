package com.socialmedia.clover_network.dto;

import com.socialmedia.clover_network.dto.req.RoleGroupSettingReq;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Data
@Builder
public class CommentDTO {
    private Long id;
    private String postId;
    private String authorId;
    private String content;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Long parentCommentId;
    private int level;
    private boolean delFlag;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class CommentInfo {
        private Long commentId;
        private String postId;
        private BaseProfile userProfile;
        private String content;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;
        private boolean isAuthor;
        private RoleGroupSettingReq currentUserRole;
    }
}
