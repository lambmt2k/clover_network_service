package com.socialmedia.clover_network.controller;

import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.service.ElasticSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/search")
public class SearchController {

    private final ElasticSearchService elasticSearchService;

    public SearchController(ElasticSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    @GetMapping("/search-by")
    public ResponseEntity<ApiResponse> searchBy(@RequestParam String keyword) {
        ApiResponse res = elasticSearchService.search(keyword);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
}
