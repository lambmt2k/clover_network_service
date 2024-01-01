package com.socialmedia.clover_network.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {
    private String oldPassword;
    private String newPassword;
    private String repeatNewPassword;
}
