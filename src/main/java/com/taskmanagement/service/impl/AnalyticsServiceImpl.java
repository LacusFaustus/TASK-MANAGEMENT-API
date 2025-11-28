package com.taskmanagement.service.impl;

import com.taskmanagement.dto.response.*;
import com.taskmanagement.entity.enums.TaskStatus;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.repository.*;
import com.taskmanagement.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TaskRepository taskRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final TaskCommentRepository taskCommentRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "workspace-analytics", key = "#workspaceId")
    public WorkspaceAnalyticsResponse getWorkspaceAnalytics(Long workspaceId, Long userId) {
        validateWorkspaceAccess(workspaceId, userId);

        Long totalTasks = taskRepository.countByWorkspaceId(workspaceId);
        Long completedTasks = taskRepository.countByWorkspaceIdAndStatus(workspaceId, TaskStatus.DONE);
        Long inProgressTasks = taskRepository.countByWorkspaceIdAndStatus(workspaceId, TaskStatus.IN_PROGRESS);
        Long overdueTasks = taskRepository.countOverdueTasksByWorkspaceId(workspaceId);
        Long totalMembers = workspaceMemberRepository.countByWorkspaceId(workspaceId);

        List<Object[]> priorityStats = taskRepository.countTasksByPriority(workspaceId);
        Map<String, Long> priorityDistribution = priorityStats.stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));

        Double avgCompletionTime = taskRepository.getAverageCompletionTime(workspaceId);

        return WorkspaceAnalyticsResponse.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .overdueTasks(overdueTasks)
                .totalMembers(totalMembers)
                .completionRate(totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0)
                .priorityDistribution(priorityDistribution)
                .averageCompletionTime(avgCompletionTime != null ? avgCompletionTime : 0.0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user-productivity", key = "#userId + '-' + #startDate + '-' + #endDate")
    public UserProductivityResponse getUserProductivity(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        Long totalTasksAssigned = taskRepository.countByAssigneeIdAndCreatedAtBetween(userId, startDate, endDate);
        Long completedTasks = taskRepository.countByAssigneeIdAndStatusAndUpdatedAtBetween(
                userId, TaskStatus.DONE, startDate, endDate);

        Long tasksCreated = taskRepository.countByAuthorIdAndCreatedAtBetween(userId, startDate, endDate);
        Long commentsWritten = taskCommentRepository.countByAuthorIdAndCreatedAtBetween(userId, startDate, endDate);

        Double avgTaskCompletionTime = taskRepository.getAverageCompletionTimeByAssignee(userId, startDate, endDate);

        List<Object[]> dailyProductivity = taskRepository.getDailyCompletedTasks(userId, startDate, endDate);
        Map<String, Long> dailyProductivityMap = dailyProductivity.stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));

        return UserProductivityResponse.builder()
                .totalTasksAssigned(totalTasksAssigned)
                .completedTasks(completedTasks)
                .tasksCreated(tasksCreated)
                .commentsWritten(commentsWritten)
                .completionRate(totalTasksAssigned > 0 ? (double) completedTasks / totalTasksAssigned * 100 : 0)
                .averageCompletionTime(avgTaskCompletionTime != null ? avgTaskCompletionTime : 0.0)
                .dailyProductivity(dailyProductivityMap)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "task-trends", key = "#workspaceId + '-' + #days")
    public List<TaskTrendResponse> getTaskTrends(Long workspaceId, int days, Long userId) {
        validateWorkspaceAccess(workspaceId, userId);

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<Object[]> trendData = taskRepository.getTaskTrends(workspaceId, startDate, endDate);

        return trendData.stream()
                .map(this::mapToTaskTrendResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "workspace-comparison", key = "#userId")
    public List<WorkspaceComparisonResponse> getWorkspaceComparison(Long userId) {
        List<WorkspaceComparisonResponse> comparisons = new ArrayList<>();

        var memberships = workspaceMemberRepository.findByUserId(userId);

        for (var membership : memberships) {
            Long workspaceId = membership.getWorkspace().getId();

            Long totalTasks = taskRepository.countByWorkspaceId(workspaceId);
            Long completedTasks = taskRepository.countByWorkspaceIdAndStatus(workspaceId, TaskStatus.DONE);
            Long activeMembers = workspaceMemberRepository.countActiveMembers(workspaceId);

            comparisons.add(WorkspaceComparisonResponse.builder()
                    .workspaceId(workspaceId)
                    .workspaceName(membership.getWorkspace().getName())
                    .totalTasks(totalTasks)
                    .completedTasks(completedTasks)
                    .activeMembers(activeMembers)
                    .completionRate(totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0)
                    .build());
        }

        return comparisons;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "team-performance", key = "#workspaceId")
    public TeamPerformanceResponse getTeamPerformance(Long workspaceId, Long userId) {
        validateWorkspaceAccess(workspaceId, userId);

        List<Object[]> teamStats = taskRepository.getTeamPerformanceStats(workspaceId);

        List<TeamMemberPerformance> memberPerformances = teamStats.stream()
                .map(this::mapToTeamMemberPerformance)
                .collect(Collectors.toList());

        Long totalTeamTasks = memberPerformances.stream()
                .mapToLong(TeamMemberPerformance::getTotalTasks)
                .sum();

        Long totalCompletedTasks = memberPerformances.stream()
                .mapToLong(TeamMemberPerformance::getCompletedTasks)
                .sum();

        return TeamPerformanceResponse.builder()
                .teamMembers(memberPerformances)
                .totalTeamTasks(totalTeamTasks)
                .totalCompletedTasks(totalCompletedTasks)
                .teamCompletionRate(totalTeamTasks > 0 ? (double) totalCompletedTasks / totalTeamTasks * 100 : 0)
                .build();
    }

    private void validateWorkspaceAccess(Long workspaceId, Long userId) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new UnauthorizedException("Access denied to workspace");
        }
    }

    private TaskTrendResponse mapToTaskTrendResponse(Object[] data) {
        return TaskTrendResponse.builder()
                .date((String) data[0])
                .tasksCreated((Long) data[1])
                .tasksCompleted((Long) data[2])
                .build();
    }

    private TeamMemberPerformance mapToTeamMemberPerformance(Object[] data) {
        Long totalTasks = (Long) data[1];
        Long completedTasks = (Long) data[2];

        return TeamMemberPerformance.builder()
                .userId((Long) data[0])
                .userName((String) data[3] + " " + data[4])
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .completionRate(totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0)
                .build();
    }
}
