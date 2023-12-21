package com.socialmedia.clover_network.dto;

import com.socialmedia.clover_network.entity.ReactionItem;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Data
@Builder
public class ReactDTO {
    private String postId;
    private ReactionItem.ReactType reactType;
    private boolean status;
}
