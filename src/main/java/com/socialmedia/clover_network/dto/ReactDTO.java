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
    private String authorId;
    private ReactionItem.ReactType reactType;
}
