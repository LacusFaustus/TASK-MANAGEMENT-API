package com.taskmanagement.mapper;

import com.taskmanagement.dto.response.CommentResponse;
import com.taskmanagement.dto.response.UserResponse;
import com.taskmanagement.entity.TaskComment;
import com.taskmanagement.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "taskId", expression = "java(comment.getTask() != null ? comment.getTask().getId() : null)")
    @Mapping(target = "author", expression = "java(mapUser(comment.getAuthor()))")
    CommentResponse toResponse(TaskComment comment);

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
