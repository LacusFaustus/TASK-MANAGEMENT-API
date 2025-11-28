package com.taskmanagement.service;

import com.taskmanagement.dto.response.*;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsService {
    WorkspaceAnalyticsResponse getWorkspaceAnalytics(Long workspaceId, Long userId);
    UserProductivityResponse getUserProductivity(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    List<TaskTrendResponse> getTaskTrends(Long workspaceId, int days, Long userId);
    List<WorkspaceComparisonResponse> getWorkspaceComparison(Long userId);
    TeamPerformanceResponse getTeamPerformance(Long workspaceId, Long userId);
}
