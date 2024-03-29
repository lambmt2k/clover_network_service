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
@Table(name = "reaction_item")
public class ReactionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long reactionId;

    @Column(name = "author_id")
    private String authorId;

    @Column(name = "react_type")
    private ReactType reactType;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "post_id")
    private String postId;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "del_flag")
    private boolean delFlag;

    public enum ReactType {
        NULL,
        LIKE;
    }
}
