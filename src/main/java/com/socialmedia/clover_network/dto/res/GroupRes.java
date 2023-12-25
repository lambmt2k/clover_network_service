package com.socialmedia.clover_network.dto.res;

import com.socialmedia.clover_network.dto.GroupItem;
import com.socialmedia.clover_network.dto.req.RoleGroupSettingReq;
import com.socialmedia.clover_network.entity.GroupEntity;
import lombok.*;

import java.util.List;
import java.util.Map;

public class GroupRes {
    @Getter
    @Setter
    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    @Builder
    @Data
    public static class ListGroupResp {
        private List<GroupEntity> groups;
        private Map<String, Long> groupLastActive;
    }


    @Getter
    @Setter
    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    @Builder
    @Data
    public static class GroupInfo {
        private GroupItem group;
        private RoleGroupSettingReq currentUserRole;
    }
}
