package com.socialmedia.clover_network.dto.req;

import com.socialmedia.clover_network.enumuration.Gender;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class UserSignUpReq {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Date dayOfBirth;
    private Gender gender;
}
