package com.taskmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberPerformance {
    private Long userId;
    private String userName;
    private Long totalTasks;
    private Long completedTasks;
    private Double completionRate;
}
