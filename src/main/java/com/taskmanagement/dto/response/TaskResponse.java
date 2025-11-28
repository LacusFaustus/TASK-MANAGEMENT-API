package com.taskmanagement.dto.response;

import com.taskmanagement.entity.enums.Priority;
import com.taskmanagement.entity.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDateTime dueDate;
    private Long workspaceId;
    private UserResponse author;
    private UserResponse assignee;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer commentCount;
    private Integer attachmentCount;
}
