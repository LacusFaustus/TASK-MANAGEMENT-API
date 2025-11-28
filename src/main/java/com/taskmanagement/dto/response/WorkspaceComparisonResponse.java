package com.taskmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceComparisonResponse {
    private Long workspaceId;
    private String workspaceName;
    private Long totalTasks;
    private Long completedTasks;
    private Long activeMembers;
    private Double completionRate;
}
