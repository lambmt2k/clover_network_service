package com.socialmedia.clover_network.dto.req;

import com.socialmedia.clover_network.entity.GroupMember;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleGroupSettingReq {
    private GroupMemberRole roleId;
    private GroupMember.GroupMemberStatus status;
    private boolean enablePost;
    private boolean enableComment;
    private boolean enableShare;
}
