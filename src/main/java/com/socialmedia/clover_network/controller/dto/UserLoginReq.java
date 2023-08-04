package com.socialmedia.clover_network.controller.dto;

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
