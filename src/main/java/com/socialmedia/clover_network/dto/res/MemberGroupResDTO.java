package com.socialmedia.clover_network.dto.res;

import com.socialmedia.clover_network.dto.BaseProfile;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
@Data
public class MemberGroupResDTO {
    private List<BaseProfile> members;
    private int total;
}
