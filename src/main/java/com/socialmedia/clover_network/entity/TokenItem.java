package com.socialmedia.clover_network.entity;

import io.micrometer.common.util.StringUtils;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
@Data
public class TokenItem implements Serializable {
    @Id
    private String tokenId;
    @Indexed
    private String userId;
    @Indexed
    private String domain;
    private int roleId;
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
        PC,
        MOBILE
    }

    public boolean isValidTokenItem() {
        return StringUtils.isNotEmpty(tokenId) && StringUtils.isNotEmpty(userId);
    }
}
