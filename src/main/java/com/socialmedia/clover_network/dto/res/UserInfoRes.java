package com.socialmedia.clover_network.dto.res;


import com.socialmedia.clover_network.enumuration.UserRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserInfoRes {
    private String userId;
    private String email;
    private String firstname;
    private String lastname;
    private String avatar;
    private String phoneNo;
    private String gender;
    private String userRole;
    private String dayOfBirth;
    private String status;
    private String userWallId;
    private boolean isConnected = false;
}
