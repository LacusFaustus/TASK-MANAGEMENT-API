package com.taskmanagement.controller;

import com.taskmanagement.dto.response.ApiResponse;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.service.TaskSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Search", description = "API для расширенного поиска задач")
public class SearchController {

    private final TaskSearchService taskSearchService;

    @GetMapping("/workspaces/{workspaceId}/tasks")
    @Operation(summary = "Поиск задач в рабочем пространстве")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> searchTasks(
            @PathVariable Long workspaceId,
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        Page<TaskResponse> response = taskSearchService.searchTasks(workspaceId, q, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success("Tasks search completed successfully", response));
    }

    @GetMapping("/tasks/{taskId}/similar")
    @Operation(summary = "Поиск похожих задач")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> findSimilarTasks(
            @PathVariable Long taskId,
            @AuthenticationPrincipal Long userId) {
        List<TaskResponse> response = taskSearchService.findSimilarTasks(taskId, userId);
        return ResponseEntity.ok(ApiResponse.success("Similar tasks found successfully", response));
    }

    @GetMapping("/workspaces/{workspaceId}/tags")
    @Operation(summary = "Поиск задач по тегам")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> findTasksByTags(
            @PathVariable Long workspaceId,
            @RequestParam List<String> tags,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        Page<TaskResponse> response = taskSearchService.findTasksByTags(workspaceId, tags, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success("Tasks by tags retrieved successfully", response));
    }
}
