package com.socialmedia.clover_network.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserLoginReq {
    private String email;
    private String password;

}
