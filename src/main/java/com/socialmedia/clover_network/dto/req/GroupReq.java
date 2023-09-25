package com.socialmedia.clover_network.dto.req;

import com.socialmedia.clover_network.entity.GroupEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GroupReq {
    private String groupName;
    private String description;
    private GroupEntity.GroupPrivacy groupPrivacy;
}
