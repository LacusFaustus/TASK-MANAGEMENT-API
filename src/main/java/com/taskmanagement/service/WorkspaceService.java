package com.taskmanagement.service;

import com.taskmanagement.dto.request.WorkspaceCreateRequest;
import com.taskmanagement.dto.request.WorkspaceUpdateRequest;
import com.taskmanagement.dto.response.WorkspaceResponse;
import com.taskmanagement.dto.response.WorkspaceStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WorkspaceService {
    WorkspaceResponse createWorkspace(WorkspaceCreateRequest request, Long userId);
    WorkspaceResponse updateWorkspace(Long workspaceId, WorkspaceUpdateRequest request, Long userId);
    WorkspaceResponse getWorkspace(Long workspaceId, Long userId);
    Page<WorkspaceResponse> getUserWorkspaces(Long userId, Pageable pageable);
    void deleteWorkspace(Long workspaceId, Long userId);
    WorkspaceResponse inviteMember(Long workspaceId, String email, String role, Long userId);
    void removeMember(Long workspaceId, Long memberId, Long userId);
    WorkspaceResponse updateMemberRole(Long workspaceId, Long memberId, String role, Long userId);
    WorkspaceStatsResponse getWorkspaceStats(Long workspaceId, Long userId);
    List<WorkspaceResponse> getRecentWorkspaces(Long userId, int limit);
}
