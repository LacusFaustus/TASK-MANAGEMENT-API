package com.taskmanagement.service.impl;

import com.taskmanagement.dto.request.TaskCreateRequest;
import com.taskmanagement.dto.request.TaskFilterRequest;
import com.taskmanagement.dto.request.TaskUpdateRequest;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.entity.Workspace;
import com.taskmanagement.entity.WorkspaceMember;
import com.taskmanagement.entity.enums.TaskStatus;
import com.taskmanagement.entity.enums.WorkspaceRole;
import com.taskmanagement.exception.BusinessException;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.mapper.TaskMapper;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.repository.WorkspaceMemberRepository;
import com.taskmanagement.repository.WorkspaceRepository;
import com.taskmanagement.repository.specification.TaskSpecification;
import com.taskmanagement.service.TaskService;
import com.taskmanagement.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taskmanagement.service.WebSocketNotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final TaskMapper taskMapper;
    private final WebSocketNotificationService webSocketNotificationService;
    private final UserActivityService userActivityService;

    @Override
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, Long userId) {
        User author = getUserById(userId);
        Workspace workspace = getWorkspaceById(request.getWorkspaceId());

        validateWorkspaceAccess(workspace.getId(), userId, WorkspaceRole.MEMBER);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .workspace(workspace)
                .author(author)
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .build();

        if (request.getAssigneeId() != null) {
            User assignee = getUserById(request.getAssigneeId());
            validateWorkspaceMember(workspace.getId(), assignee.getId());
            task.setAssignee(assignee);
        }

        if (request.getTags() != null) {
            task.setTags(request.getTags());
        }

        Task savedTask = taskRepository.save(task);
        log.info("Task created with id: {} by user: {}", savedTask.getId(), userId);

        // Записываем активность
        userActivityService.recordUserActivity(
                userId,
                UserActivityService.ActivityType.TASK_EDIT,
                savedTask.getId()
        );

        // Уведомляем через WebSocket
        webSocketNotificationService.broadcastTaskUpdate(
                savedTask.getWorkspace().getId(),
                savedTask.getId(),
                "CREATED"
        );

        return taskMapper.toResponse(savedTask);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request, Long userId) {
        Task task = getTaskById(taskId);
        validateTaskAccess(task, userId, true);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getTags() != null) {
            task.setTags(request.getTags());
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated with id: {} by user: {}", taskId, userId);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId, Long userId) {
        Task task = getTaskById(taskId);
        validateTaskAccess(task, userId, false);

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(TaskFilterRequest filter, Pageable pageable, Long userId) {
        Specification<Task> spec = TaskSpecification.withFilter(filter, userId);
        Page<Task> tasks = taskRepository.findAll(spec, pageable);

        return tasks.map(taskMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = getTaskById(taskId);
        validateTaskAccess(task, userId, true);

        taskRepository.delete(task);
        log.info("Task deleted with id: {} by user: {}", taskId, userId);
    }

    @Override
    @Transactional
    public TaskResponse changeStatus(Long taskId, String status, Long userId) {
        Task task = getTaskById(taskId);
        validateTaskAccess(task, userId, true);

        try {
            TaskStatus newStatus = TaskStatus.valueOf(status.toUpperCase());
            task.setStatus(newStatus);
            Task updatedTask = taskRepository.save(task);

            log.info("Task status changed to: {} for task: {} by user: {}", status, taskId, userId);

            // Уведомляем об изменении статуса
            webSocketNotificationService.broadcastTaskUpdate(
                    task.getWorkspace().getId(),
                    taskId,
                    "STATUS_CHANGED"
            );

            return taskMapper.toResponse(updatedTask);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid task status: " + status);
        }
    }

    @Override
    @Transactional
    public TaskResponse assignTask(Long taskId, Long assigneeId, Long userId) {
        Task task = getTaskById(taskId);
        validateTaskAccess(task, userId, true);

        User assignee = getUserById(assigneeId);
        validateWorkspaceMember(task.getWorkspace().getId(), assigneeId);

        task.setAssignee(assignee);
        Task updatedTask = taskRepository.save(task);

        log.info("Task assigned to user: {} for task: {} by user: {}", assigneeId, taskId, userId);

        // Отправляем уведомление назначенному пользователю
        webSocketNotificationService.sendTaskAssignmentNotification(
                taskId,
                assigneeId,
                task.getTitle()
        );

        // Записываем активность
        userActivityService.recordUserActivity(
                userId,
                UserActivityService.ActivityType.TASK_EDIT,
                taskId
        );

        return taskMapper.toResponse(updatedTask);
    }

    private Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Workspace getWorkspaceById(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));
    }

    private void validateWorkspaceAccess(Long workspaceId, Long userId, WorkspaceRole requiredRole) {
        WorkspaceMember membership = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new UnauthorizedException("Access denied to workspace"));

        if (!hasRequiredRole(membership.getRole(), requiredRole)) {
            throw new UnauthorizedException("Insufficient permissions");
        }
    }

    private void validateTaskAccess(Task task, Long userId, boolean requireWrite) {
        WorkspaceMember membership = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(task.getWorkspace().getId(), userId)
                .orElseThrow(() -> new UnauthorizedException("Access denied to task"));

        if (requireWrite && membership.getRole() == WorkspaceRole.VIEWER) {
            throw new UnauthorizedException("Viewer cannot modify tasks");
        }
    }

    private void validateWorkspaceMember(Long workspaceId, Long userId) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new BusinessException("User is not a member of this workspace");
        }
    }

    private boolean hasRequiredRole(WorkspaceRole userRole, WorkspaceRole requiredRole) {
        return userRole.ordinal() <= requiredRole.ordinal();
    }
}
