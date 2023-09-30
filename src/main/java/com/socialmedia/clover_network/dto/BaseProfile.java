package com.socialmedia.clover_network.dto;

import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BaseProfile implements Serializable {
    private String userId;
    private GroupMemberRole groupRole;
    private String displayName;
    private String avatarImgUrl;
    private String phoneNo;
    private String email;
    private String userWallId;
}
