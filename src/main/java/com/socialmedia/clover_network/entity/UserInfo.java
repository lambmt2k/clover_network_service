package com.socialmedia.clover_network.entity;

import com.socialmedia.clover_network.constant.CommonConstant;
import com.socialmedia.clover_network.enumuration.AccountType;
import com.socialmedia.clover_network.enumuration.Gender;
import com.socialmedia.clover_network.enumuration.UserRole;
import com.socialmedia.clover_network.enumuration.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_info")
public class UserInfo {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "avatar_img_url", columnDefinition = "TEXT")
    private String avatarImgUrl;

    @Column(name = "email")
    private String email;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "day_of_birth")
    private Date dayOfBirth;

    @Column(name = "phone_no")
    private String phoneNo;

    @Column(name = "gender")
    private Gender gender;

    @Column(name = "account_type")
    private AccountType accountType;

    @Column(name = "user_role")
    private UserRole userRole;

    @Column(name = "status")
    private UserStatus status;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

}
