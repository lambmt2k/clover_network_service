package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.ParamSearch;
import com.socialmedia.clover_network.dto.res.ApiResponse;

public interface ElasticSearchService {
    ApiResponse search(String keyword);
}
