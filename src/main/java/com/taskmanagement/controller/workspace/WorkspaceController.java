package com.taskmanagement.controller.workspace;

import com.taskmanagement.dto.request.WorkspaceCreateRequest;
import com.taskmanagement.dto.request.WorkspaceUpdateRequest;
import com.taskmanagement.dto.response.ApiResponse;
import com.taskmanagement.dto.response.WorkspaceResponse;
import com.taskmanagement.dto.response.WorkspaceStatsResponse;
import com.taskmanagement.security.CustomUserDetails; // Добавьте этот импорт
import com.taskmanagement.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Workspaces", description = "API для управления рабочими пространствами")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @Operation(summary = "Создание нового рабочего пространства")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @Valid @RequestBody WorkspaceCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) { // Изменили здесь

        Long userId = userDetails.getUserId(); // Получаем ID из CustomUserDetails
        WorkspaceResponse response = workspaceService.createWorkspace(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace created successfully", response));
    }

    @PutMapping("/{workspaceId}")
    @Operation(summary = "Обновление рабочего пространства")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @PathVariable Long workspaceId,
            @Valid @RequestBody WorkspaceUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        WorkspaceResponse response = workspaceService.updateWorkspace(workspaceId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace updated successfully", response));
    }

    @GetMapping("/{workspaceId}")
    @Operation(summary = "Получение рабочего пространства по ID")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        WorkspaceResponse response = workspaceService.getWorkspace(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary = "Получение списка рабочих пространств пользователя")
    public ResponseEntity<ApiResponse<Page<WorkspaceResponse>>> getUserWorkspaces(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        Long userId = userDetails.getUserId();
        Page<WorkspaceResponse> response = workspaceService.getUserWorkspaces(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Workspaces retrieved successfully", response));
    }

    @GetMapping("/recent")
    @Operation(summary = "Получение недавних рабочих пространств")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getRecentWorkspaces(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "5") int limit) {

        Long userId = userDetails.getUserId();
        List<WorkspaceResponse> response = workspaceService.getRecentWorkspaces(userId, limit);
        return ResponseEntity.ok(ApiResponse.success("Recent workspaces retrieved successfully", response));
    }

    @DeleteMapping("/{workspaceId}")
    @Operation(summary = "Удаление рабочего пространства")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        workspaceService.deleteWorkspace(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace deleted successfully"));
    }

    @GetMapping("/{workspaceId}/stats")
    @Operation(summary = "Получение статистики рабочего пространства")
    public ResponseEntity<ApiResponse<WorkspaceStatsResponse>> getWorkspaceStats(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        WorkspaceStatsResponse response = workspaceService.getWorkspaceStats(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Workspace stats retrieved successfully", response));
    }
}
