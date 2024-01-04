package com.socialmedia.clover_network.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveMemberGroup {
    private String groupId;
    private String userId;
}
