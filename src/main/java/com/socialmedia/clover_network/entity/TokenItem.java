package com.socialmedia.clover_network.entity;

import com.socialmedia.clover_network.enumuration.UserRole;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
@Data
@Table(name = "user_info")
public class TokenItem implements Serializable {
    @Id
    private String tokenId;
    @Indexed
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
