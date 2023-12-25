package com.socialmedia.clover_network.mapper;

import com.socialmedia.clover_network.dto.FeedItem;
import com.socialmedia.clover_network.dto.GroupItem;
import com.socialmedia.clover_network.entity.GroupEntity;
import com.socialmedia.clover_network.entity.PostItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupEntityMapper {
    GroupEntityMapper INSTANCE = Mappers.getMapper(GroupEntityMapper.class);

    GroupItem toDTO(GroupEntity groupEntity);
}
