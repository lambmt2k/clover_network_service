package com.socialmedia.clover_network.mapper;

import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.entity.PostItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostItemMapper {
    PostItemMapper INSTANCE = Mappers.getMapper(PostItemMapper.class);

    PostItem toEntity(FeedItem feedItem);
    FeedItem toDTO(PostItem postItem);
    List<FeedItem> toDTOS(List<PostItem> postItems);
}
