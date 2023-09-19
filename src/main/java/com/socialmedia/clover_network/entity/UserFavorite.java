package com.socialmedia.clover_network.entity;

import com.socialmedia.clover_network.enumuration.Favorite;
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
@Table(name = "user_favorite")
public class UserFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "favorite_id")
    private Favorite favoriteId;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "del_flag")
    private boolean delFlag;
}
