package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.dto.req.GroupReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.service.GroupService;
import com.socialmedia.clover_network.util.GenIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GroupServiceImpl implements GroupService {
    private final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    private final GenIDUtil genIDUtil;

    public GroupServiceImpl(GenIDUtil genIDUtil) {
        this.genIDUtil = genIDUtil;
    }

    @Override
    public ApiResponse createNewGroup(GroupReq groupReq) {
        ApiResponse res = new ApiResponse();
        String userId = AuthenticationHelper.getUserIdFromContext();
        if (userId != null) {
            LocalDateTime now = LocalDateTime.now();
            String groupId = genIDUtil.genId();
            GroupEntity groupEntity = new GroupEntity();
            groupEntity.setGroupId(groupId);
            groupEntity.setGroupName(groupReq.getGroupName());
            groupEntity.setGroupDesc(groupReq.getDescription());
            groupEntity.setCreatedTime(now);
        }
        return res;
    }
}
