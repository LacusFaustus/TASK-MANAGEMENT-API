package com.taskmanagement.controller.task;

import com.taskmanagement.dto.request.CommentCreateRequest;
import com.taskmanagement.dto.response.ApiResponse;
import com.taskmanagement.dto.response.CommentResponse;
import com.taskmanagement.service.TaskCommentService;
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
@RequestMapping("/api/v1/tasks/{taskId}/comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Task Comments", description = "API для управления комментариями задач")
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    @PostMapping
    @Operation(summary = "Добавление комментария к задаче")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal Long userId) {
        CommentResponse response = taskCommentService.addComment(taskId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", response));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Обновление комментария")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @RequestParam String content,
            @AuthenticationPrincipal Long userId) {
        CommentResponse response = taskCommentService.updateComment(commentId, content, userId);
        return ResponseEntity.ok(ApiResponse.success("Comment updated successfully", response));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Удаление комментария")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long userId) {
        taskCommentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully"));
    }

    @GetMapping
    @Operation(summary = "Получение комментариев задачи")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getTaskComments(
            @PathVariable Long taskId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        Page<CommentResponse> response = taskCommentService.getTaskComments(taskId, pageable, userId);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", response));
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Получение комментария по ID")
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long userId) {
        CommentResponse response = taskCommentService.getComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.success("Comment retrieved successfully", response));
    }
}
