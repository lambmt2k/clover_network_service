package com.socialmedia.clover_network.entity;


import com.socialmedia.clover_network.enumuration.UserRole;
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
@Table(name = "login_history")
public class LoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_role")
    private UserRole userRole;

    @Column(name = "platform")
    private TokenItem.PLATFORM platform;

    @Column(name = "os")
    private TokenItem.OS os;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "time_login")
    private LocalDateTime timeLogin;
}
