package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.req.GroupReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;

public interface GroupService {
    ApiResponse createNewGroup(GroupReq groupReq);
}
