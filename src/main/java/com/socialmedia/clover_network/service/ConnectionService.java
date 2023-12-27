package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.ConnectionDTO;
import com.socialmedia.clover_network.dto.res.ApiResponse;

public interface ConnectionService {
    ApiResponse connectToUser(ConnectionDTO.ConnectUserItem connectUserItem);

    boolean checkAConnectB(String userAId, String userBId);
}
