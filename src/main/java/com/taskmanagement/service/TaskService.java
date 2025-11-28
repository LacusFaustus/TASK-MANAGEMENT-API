package com.taskmanagement.service;

import com.taskmanagement.dto.request.TaskCreateRequest;
import com.taskmanagement.dto.request.TaskFilterRequest;
import com.taskmanagement.dto.request.TaskUpdateRequest;
import com.taskmanagement.dto.response.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskResponse createTask(TaskCreateRequest request, Long userId);
    TaskResponse updateTask(Long taskId, TaskUpdateRequest request, Long userId);
    TaskResponse getTask(Long taskId, Long userId);
    Page<TaskResponse> getTasks(TaskFilterRequest filter, Pageable pageable, Long userId);
    void deleteTask(Long taskId, Long userId);
    TaskResponse changeStatus(Long taskId, String status, Long userId);
    TaskResponse assignTask(Long taskId, Long assigneeId, Long userId);
}
