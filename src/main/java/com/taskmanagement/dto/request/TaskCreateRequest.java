package com.taskmanagement.dto.request;

import com.taskmanagement.entity.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    @NotNull(message = "Workspace ID is required")
    private Long workspaceId;

    private Long assigneeId;
    private Priority priority;
    private LocalDateTime dueDate;
    private List<String> tags;
}
