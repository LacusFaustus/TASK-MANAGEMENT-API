package com.taskmanagement.service;

import com.taskmanagement.dto.response.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskSearchService {
    Page<TaskResponse> searchTasks(Long workspaceId, String query, Pageable pageable, Long userId);
    List<TaskResponse> findSimilarTasks(Long taskId, Long userId);
    Page<TaskResponse> findTasksByTags(Long workspaceId, List<String> tags, Pageable pageable, Long userId);
}
