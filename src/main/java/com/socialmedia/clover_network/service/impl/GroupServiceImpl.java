package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.dto.req.GroupReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.service.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService {
    private final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);
    @Override
    public ApiResponse createNewGroup(GroupReq groupReq) {
        ApiResponse res = new ApiResponse();

        return res;
    }
}
