package com.taskmanagement.service;

import com.taskmanagement.entity.enums.ExportFormat;

import java.io.OutputStream;

public interface ExportService {
    void exportWorkspaceTasks(Long workspaceId, ExportFormat format, OutputStream outputStream, Long userId);
    void exportUserTasks(Long userId, ExportFormat format, OutputStream outputStream);
    void exportWorkspaceAnalytics(Long workspaceId, ExportFormat format, OutputStream outputStream, Long userId);
}
