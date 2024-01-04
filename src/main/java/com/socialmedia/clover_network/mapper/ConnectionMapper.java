package com.socialmedia.clover_network.mapper;

import com.socialmedia.clover_network.dto.ConnectionDTO;
import com.socialmedia.clover_network.entity.Connection;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ConnectionMapper {
    ConnectionMapper INSTANCE = Mappers.getMapper(ConnectionMapper.class);

    ConnectionDTO toDTO(Connection connection);

    Connection toEntity(ConnectionDTO dto);
}
