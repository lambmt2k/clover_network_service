package com.socialmedia.clover_network.service.impl;

import com.google.gson.JsonSyntaxException;
import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.service.ElasticSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    private final Logger logger = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);
    @Override
    public ApiResponse search(String keyword) {
        ApiResponse res = new ApiResponse();
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (currentUserId != null) {

        } else{
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
        }
        return res;
    }
}
