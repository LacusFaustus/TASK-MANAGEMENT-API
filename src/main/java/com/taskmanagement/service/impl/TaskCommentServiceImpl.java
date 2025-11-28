package com.taskmanagement.service.impl;

import com.taskmanagement.dto.request.CommentCreateRequest;
import com.taskmanagement.dto.response.CommentResponse;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.TaskComment;
import com.taskmanagement.entity.User;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.mapper.CommentMapper;
import com.taskmanagement.repository.TaskCommentRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.repository.WorkspaceMemberRepository;
import com.taskmanagement.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentResponse addComment(Long taskId, CommentCreateRequest request, Long userId) {
        Task task = getTaskById(taskId);
        User author = getUserById(userId);

        validateTaskAccess(task.getWorkspace().getId(), userId);

        TaskComment comment = TaskComment.builder()
                .content(request.getContent())
                .task(task)
                .author(author)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TaskComment savedComment = commentRepository.save(comment);
        log.info("Comment added to task: {} by user: {}", taskId, userId);

        return commentMapper.toResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, String content, Long userId) {
        TaskComment comment = getCommentById(commentId);
        validateCommentOwnership(comment, userId);

        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());

        TaskComment updatedComment = commentRepository.save(comment);
        log.info("Comment updated: {} by user: {}", commentId, userId);

        return commentMapper.toResponse(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        TaskComment comment = getCommentById(commentId);

        // Allow deletion by comment author or workspace admin
        if (!comment.getAuthor().getId().equals(userId)) {
            validateWorkspaceAdminAccess(comment.getTask().getWorkspace().getId(), userId);
        }

        commentRepository.delete(comment);
        log.info("Comment deleted: {} by user: {}", commentId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getTaskComments(Long taskId, Pageable pageable, Long userId) {
        Task task = getTaskById(taskId);
        validateTaskAccess(task.getWorkspace().getId(), userId);

        Page<TaskComment> comments = commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable);
        return comments.map(commentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getComment(Long commentId, Long userId) {
        TaskComment comment = getCommentById(commentId);
        validateTaskAccess(comment.getTask().getWorkspace().getId(), userId);

        return commentMapper.toResponse(comment);
    }

    private Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private TaskComment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
    }

    private void validateTaskAccess(Long workspaceId, Long userId) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new UnauthorizedException("Access denied to task");
        }
    }

    private void validateWorkspaceAdminAccess(Long workspaceId, Long userId) {
        workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .filter(member -> member.getRole().ordinal() <= 1) // OWNER or ADMIN
                .orElseThrow(() -> new UnauthorizedException("Insufficient permissions for this operation"));
    }

    private void validateCommentOwnership(TaskComment comment, Long userId) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException("Only comment author can modify this comment");
        }
    }
}
