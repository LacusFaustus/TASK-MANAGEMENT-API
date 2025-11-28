package com.taskmanagement.controller;

import com.taskmanagement.dto.response.*;
import com.taskmanagement.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Analytics", description = "API для аналитики и отчетов")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/workspaces/{workspaceId}")
    @Operation(summary = "Получение аналитики рабочего пространства")
    public ResponseEntity<ApiResponse<WorkspaceAnalyticsResponse>> getWorkspaceAnalytics(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal Long userId) {
        WorkspaceAnalyticsResponse response = analyticsService.getWorkspaceAnalytics(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace analytics retrieved successfully", response));
    }

    @GetMapping("/user/productivity")
    @Operation(summary = "Получение продуктивности пользователя")
    public ResponseEntity<ApiResponse<UserProductivityResponse>> getUserProductivity(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        UserProductivityResponse response = analyticsService.getUserProductivity(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("User productivity retrieved successfully", response));
    }

    @GetMapping("/workspaces/{workspaceId}/trends")
    @Operation(summary = "Получение трендов задач")
    public ResponseEntity<ApiResponse<List<TaskTrendResponse>>> getTaskTrends(
            @PathVariable Long workspaceId,
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal Long userId) {
        List<TaskTrendResponse> response = analyticsService.getTaskTrends(workspaceId, days, userId);
        return ResponseEntity.ok(ApiResponse.success("Task trends retrieved successfully", response));
    }

    @GetMapping("/workspaces/comparison")
    @Operation(summary = "Сравнение рабочих пространств")
    public ResponseEntity<ApiResponse<List<WorkspaceComparisonResponse>>> getWorkspaceComparison(
            @AuthenticationPrincipal Long userId) {
        List<WorkspaceComparisonResponse> response = analyticsService.getWorkspaceComparison(userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace comparison retrieved successfully", response));
    }

    @GetMapping("/workspaces/{workspaceId}/team-performance")
    @Operation(summary = "Производительность команды")
    public ResponseEntity<ApiResponse<TeamPerformanceResponse>> getTeamPerformance(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal Long userId) {
        TeamPerformanceResponse response = analyticsService.getTeamPerformance(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Team performance retrieved successfully", response));
    }
}
