package com.socialmedia.clover_network.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FirebaseTokenUserItem {
    private String userId;
    private String token;
}
