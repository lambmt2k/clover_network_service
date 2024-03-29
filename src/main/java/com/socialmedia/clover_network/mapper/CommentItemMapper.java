package com.socialmedia.clover_network.mapper;

import com.socialmedia.clover_network.dto.CommentDTO;
import com.socialmedia.clover_network.entity.CommentItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentItemMapper {
    CommentItemMapper INSTANCE = Mappers.getMapper(CommentItemMapper.class);

    CommentItem toEntity(CommentDTO commentDTO);

    CommentDTO toDTO(CommentItem commentItem);

    List<CommentDTO> toDTOS(List<CommentItem> commentItems);

}
