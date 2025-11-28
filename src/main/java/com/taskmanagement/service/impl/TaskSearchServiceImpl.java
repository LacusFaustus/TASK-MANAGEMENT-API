package com.taskmanagement.service.impl;

import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.entity.Task;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.mapper.TaskMapper;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.WorkspaceMemberRepository;
import com.taskmanagement.service.TaskSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSearchServiceImpl implements TaskSearchService {

    private final TaskRepository taskRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> searchTasks(Long workspaceId, String query, Pageable pageable, Long userId) {
        validateWorkspaceAccess(workspaceId, userId);

        Page<Task> tasks = taskRepository.fullTextSearch(workspaceId, query, pageable);
        return tasks.map(taskMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findSimilarTasks(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        validateWorkspaceAccess(task.getWorkspace().getId(), userId);

        // Find tasks with similar tags in the same workspace
        List<Task> similarTasks = taskRepository.findSimilarTasks(
                task.getWorkspace().getId(),
                task.getTags(),
                taskId
        ).stream().limit(5).collect(Collectors.toList());

        return similarTasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> findTasksByTags(Long workspaceId, List<String> tags, Pageable pageable, Long userId) {
        validateWorkspaceAccess(workspaceId, userId);

        Page<Task> tasks = taskRepository.findByWorkspaceIdAndTagsIn(workspaceId, tags, pageable);
        return tasks.map(taskMapper::toResponse);
    }

    private void validateWorkspaceAccess(Long workspaceId, Long userId) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new UnauthorizedException("Access denied to workspace");
        }
    }
}
