package com.taskmanagement.mapper;

import com.taskmanagement.dto.response.NotificationResponse;
import com.taskmanagement.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstName", target = "userFirstName")
    @Mapping(source = "user.lastName", target = "userLastName")
    NotificationResponse toResponse(Notification notification);
}
