package com.taskmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProductivityResponse {
    private Long totalTasksAssigned;
    private Long completedTasks;
    private Long tasksCreated;
    private Long commentsWritten;
    private Double completionRate;
    private Double averageCompletionTime; // in hours
    private Map<String, Long> dailyProductivity;
}
