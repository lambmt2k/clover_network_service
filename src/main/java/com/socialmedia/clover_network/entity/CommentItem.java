package com.socialmedia.clover_network.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comment_item")
public class CommentItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long commentId;

    @Column(name = "author_id")
    private String authorId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "post_id")
    private String postId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "level")
    private int level;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "del_flag")
    private boolean delFlag;

    @JsonIgnore
    public boolean isValidForUpdate() {
        if (this.commentId == null) {
            return false;
        }
        if (StringUtils.isEmpty(this.postId)) {
            return false;
        }
        if (StringUtils.isEmpty(this.content)) {
            return false;
        }
        return true;
    }
}
