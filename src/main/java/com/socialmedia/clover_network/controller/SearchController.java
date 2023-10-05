package com.socialmedia.clover_network.controller;

import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.service.ElasticSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/search")
public class SearchController {

    private final ElasticSearchService elasticSearchService;

    public SearchController(ElasticSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    @PostMapping("/search-by")
    public ResponseEntity<ApiResponse> searchBy(@RequestBody String keyword) {
        ApiResponse res = elasticSearchService.search(keyword);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
}
