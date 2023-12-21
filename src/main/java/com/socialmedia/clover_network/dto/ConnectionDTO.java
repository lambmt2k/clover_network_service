package com.socialmedia.clover_network.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionDTO {
    private String id;
    private String userId;
    private String userIdConnected;
    private LocalDateTime timeConnect;
    private LocalDateTime timeDisconnect;
    private boolean connectStatus;


    @Getter
    @Setter
    public static class ConnectUserItem {
        private String targetUserId;
        private boolean status;
    }
}
