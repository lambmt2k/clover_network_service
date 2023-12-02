package com.socialmedia.clover_network.dto.res;

import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.entity.GroupEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SearchRes {
    List<UserInfoRes> users;
    List<GroupEntity> groups;
    List<FeedItem> feeds;
}
