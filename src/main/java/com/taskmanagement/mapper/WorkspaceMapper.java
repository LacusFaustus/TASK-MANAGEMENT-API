package com.taskmanagement.mapper;

import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.dto.response.WorkspaceResponse;
import com.taskmanagement.entity.Workspace;
import com.taskmanagement.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkspaceMapper {

    @Mapping(target = "owner", expression = "java(mapUser(workspace.getOwner()))")
    @Mapping(target = "memberCount", expression = "java(workspace.getMembers() != null ? workspace.getMembers().size() : 0)")
    @Mapping(target = "taskCount", expression = "java(workspace.getTasks() != null ? workspace.getTasks().size() : 0)")
    WorkspaceResponse toResponse(Workspace workspace);

    default UserResponse mapUser(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
