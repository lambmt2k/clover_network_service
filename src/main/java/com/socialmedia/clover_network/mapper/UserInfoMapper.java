package com.socialmedia.clover_network.mapper;

import com.socialmedia.clover_network.dto.res.UserInfoRes;
import com.socialmedia.clover_network.entity.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserInfoMapper {
    UserInfoMapper INSTANCE = Mappers.getMapper(UserInfoMapper.class);

    UserInfoRes toDTO(UserInfo userInfo);

    List<UserInfoRes> toDTOS(List<UserInfo> userInfos);
}
