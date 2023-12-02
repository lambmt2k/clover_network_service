package com.socialmedia.clover_network.controller;

import com.socialmedia.clover_network.dto.ConnectionDTO;
import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.service.ConnectionService;
import com.socialmedia.clover_network.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/connection")
public class ConnectionController {

    private final Logger logger = LoggerFactory.getLogger(ConnectionController.class);

    @Autowired
    ConnectionService connectionService;

    @PostMapping(value = "/connect-user", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ApiResponse> connectUser(@RequestBody ConnectionDTO.ConnectUserItem connectUserItem) {
        try {
            ApiResponse res = connectionService.connectToUser(connectUserItem);
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
