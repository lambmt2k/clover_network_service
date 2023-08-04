package com.socialmedia.clover_network.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserAuthDto {
    private String email;
    private String password;
}
