package com.socialmedia.clover_network.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDTO {
    private String email;
    private String newPassword;
    private String repeatNewPassword;
    private String otp;
}
