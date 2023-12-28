package com.socialmedia.clover_network.mapper;

import com.socialmedia.clover_network.dto.GroupItem;
import com.socialmedia.clover_network.entity.GroupEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GroupEntityMapper {
    GroupEntityMapper INSTANCE = Mappers.getMapper(GroupEntityMapper.class);

    GroupItem toDTO(GroupEntity groupEntity);
}
