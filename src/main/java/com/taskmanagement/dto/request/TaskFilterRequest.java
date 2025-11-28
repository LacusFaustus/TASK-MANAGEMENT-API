package com.taskmanagement.dto.request;

import com.taskmanagement.entity.enums.Priority;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskFilterRequest {
    private Long workspaceId;
    private List<String> status;
    private List<Priority> priority;
    private Long assigneeId;
    private Long authorId;
    private List<String> tags;
    private LocalDateTime dueDateFrom;
    private LocalDateTime dueDateTo;
    private LocalDateTime createdAtFrom;
    private LocalDateTime createdAtTo;
    private String search;
    private Boolean overdue;
}
