package com.taskmanagement.dto.request;

import com.taskmanagement.service.UserActivityService;
import lombok.Data;

@Data
public class UserActivityRequest {
    private Long userId;
    private UserActivityService.ActivityType activityType;
    private Long entityId;
}
