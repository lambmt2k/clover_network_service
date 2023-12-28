package com.socialmedia.clover_network.dto.res;

import com.socialmedia.clover_network.dto.BaseProfile;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserProfileDTO {
    private UserInfoRes userInfo;
    private int totalConnect;
    private int totalConnector;

    @Getter
    @Setter
    public static class ListUserConnectDTO {
        private List<BaseProfile> userProfiles;
        private int total;
    }
}
