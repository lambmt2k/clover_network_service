package com.socialmedia.clover_network.entity;

import com.socialmedia.clover_network.enumuration.UserRole;
import lombok.*;
import org.apache.commons.lang3.StringUtils;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
@Entity
@Table(name = "token_item")
public class TokenItem implements Serializable {
    @Id
    private String tokenId;
    private String userId;
    private UserRole userRole;
    private String deviceId;
    private String deviceName;
    private PLATFORM platform;
    private OS os;
    private String userAgent;
    private String userIp;
    private LocalDateTime createdTime;
    private LocalDateTime expireTime;
    private boolean delFlag;

    public static enum OS {
        INVALID,
        WINDOWS,
        MAC,
        LINUX,
        ANDROID,
        IOS
    }

    public static enum PLATFORM {
        INVALID,
        WEB,
        MOBILE
    }

    public boolean isValidTokenItem() {
        return StringUtils.isNotEmpty(tokenId) && StringUtils.isNotEmpty(userId);
    }
}
