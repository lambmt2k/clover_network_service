package com.socialmedia.clover_network.dto.res;

import com.socialmedia.clover_network.enumuration.UserRole;
import com.socialmedia.clover_network.enumuration.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRes {
    private String userId;
    private String tokenId;
    private UserRole userRole;
    private UserStatus userStatus;
    private LocalDateTime expireTime;
}
