package com.taskmanagement.mapper;

import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "workspaceId", expression = "java(task.getWorkspace() != null ? task.getWorkspace().getId() : null)")
    @Mapping(target = "author", expression = "java(mapUser(task.getAuthor()))")
    @Mapping(target = "assignee", expression = "java(mapUser(task.getAssignee()))")
    @Mapping(target = "commentCount", expression = "java(task.getComments() != null ? task.getComments().size() : 0)")
    @Mapping(target = "attachmentCount", expression = "java(task.getAttachments() != null ? task.getAttachments().size() : 0)")
    TaskResponse toResponse(Task task);

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
