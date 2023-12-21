package com.socialmedia.clover_network.entity;

import com.socialmedia.clover_network.enumuration.Favorite;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
@Table(name = "connection")
public class Connection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_id_connected")
    private String userIdConnected;

    @Column(name = "connect_status")
    private boolean connectStatus;

    @Column(name = "time_connect")
    private LocalDateTime timeConnect;

    @Column(name = "time_disconnect")
    private LocalDateTime timeDisconnect;

    @Column(name = "del_flag")
    private boolean delFlag;
}
