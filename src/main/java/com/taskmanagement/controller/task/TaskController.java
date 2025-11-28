package com.taskmanagement.controller.task;

import com.taskmanagement.dto.request.TaskCreateRequest;
import com.taskmanagement.dto.request.TaskFilterRequest;
import com.taskmanagement.dto.request.TaskUpdateRequest;
import com.taskmanagement.dto.response.ApiResponse;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.security.CustomUserDetails;
import com.taskmanagement.service.TaskService;
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

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tasks", description = "API для управления задачами")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Создание новой задачи")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        TaskResponse response = taskService.createTask(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Task created successfully", response));
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Обновление задачи")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        TaskResponse response = taskService.updateTask(taskId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", response));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Получение задачи по ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        TaskResponse response = taskService.getTask(taskId, userId);
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary = "Получение списка задач с фильтрацией")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasks(
            TaskFilterRequest filter,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        Page<TaskResponse> response = taskService.getTasks(filter, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", response));
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Удаление задачи")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        taskService.deleteTask(taskId, userId);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
    }

    @PutMapping("/{taskId}/status")
    @Operation(summary = "Изменение статуса задачи")
    public ResponseEntity<ApiResponse<TaskResponse>> changeStatus(
            @PathVariable Long taskId,
            @RequestParam String status,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        TaskResponse response = taskService.changeStatus(taskId, status, userId);
        return ResponseEntity.ok(ApiResponse.success("Task status updated successfully", response));
    }

    @PostMapping("/{taskId}/assign")
    @Operation(summary = "Назначение исполнителя задачи")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long taskId,
            @RequestParam Long assigneeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        TaskResponse response = taskService.assignTask(taskId, assigneeId, userId);
        return ResponseEntity.ok(ApiResponse.success("Task assigned successfully", response));
    }
}
