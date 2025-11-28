package com.taskmanagement.repository;

import com.taskmanagement.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomTaskRepository {
    Page<Task> fullTextSearch(Long workspaceId, String query, Pageable pageable);
    List<Task> findSimilarTasks(Long workspaceId, List<String> tags, Long excludeTaskId);
    Page<Task> findByWorkspaceIdAndTagsIn(Long workspaceId, List<String> tags, Pageable pageable);
}
