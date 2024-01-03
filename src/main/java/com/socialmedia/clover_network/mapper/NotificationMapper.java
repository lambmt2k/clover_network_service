package com.socialmedia.clover_network.mapper;

import com.socialmedia.clover_network.dto.NotificationItem;
import com.socialmedia.clover_network.entity.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    NotificationEntity toEntity(NotificationItem dto);

    NotificationItem toDTO(NotificationEntity entity);
}
