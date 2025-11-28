package com.taskmanagement.mapper;

import com.taskmanagement.dto.response.UserProfileResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "workspaceCount", expression = "java(user.getWorkspaceMemberships() != null ? user.getWorkspaceMemberships().size() : 0)")
    UserProfileResponse toProfileResponse(User user);
}
