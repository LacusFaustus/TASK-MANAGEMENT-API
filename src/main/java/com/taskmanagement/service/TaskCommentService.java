package com.taskmanagement.service;

import com.taskmanagement.dto.request.CommentCreateRequest;
import com.taskmanagement.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskCommentService {
    CommentResponse addComment(Long taskId, CommentCreateRequest request, Long userId);
    CommentResponse updateComment(Long commentId, String content, Long userId);
    void deleteComment(Long commentId, Long userId);
    Page<CommentResponse> getTaskComments(Long taskId, Pageable pageable, Long userId);
    CommentResponse getComment(Long commentId, Long userId);
}
