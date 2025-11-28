package com.taskmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamPerformanceResponse {
    private List<TeamMemberPerformance> teamMembers;
    private Long totalTeamTasks;
    private Long totalCompletedTasks;
    private Double teamCompletionRate;
}
