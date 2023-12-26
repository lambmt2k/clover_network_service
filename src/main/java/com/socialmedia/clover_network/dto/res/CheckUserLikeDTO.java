package com.socialmedia.clover_network.dto.res;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CheckUserLikeDTO {
    private String feedId;
    private Integer totalLike;
    private boolean isCurrentUserLike = false;
}
