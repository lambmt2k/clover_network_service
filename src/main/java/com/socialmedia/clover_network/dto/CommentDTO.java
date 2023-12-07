package com.socialmedia.clover_network.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Data
@Builder
public class CommentDTO {
    private Long commentId;
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
        private BaseProfile authorProfile;
        private String content;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;
        private Long parentCommentId;
        private int level;
        private boolean isAuthor;
    }
}
