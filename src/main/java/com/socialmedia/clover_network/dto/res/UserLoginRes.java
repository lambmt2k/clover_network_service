package com.socialmedia.clover_network.dto.res;

import com.socialmedia.clover_network.dto.UserDTO;
import com.socialmedia.clover_network.enumuration.UserRole;
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
    private LocalDateTime expireTime;
}
