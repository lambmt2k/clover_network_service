package com.socialmedia.clover_network.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

@Entity
@Table(name = "feed_group")
@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Data
@Builder
public class FeedGroupEntity implements Serializable {
    private static Gson gson = new Gson();

    @Id
    private String key;
    @Column(columnDefinition = "text")
    private String value;

    @JsonIgnore
    public List<String> getListFeedId() {
        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> feedIds = gson.fromJson(this.value, listType);

        return feedIds;
    }
}
