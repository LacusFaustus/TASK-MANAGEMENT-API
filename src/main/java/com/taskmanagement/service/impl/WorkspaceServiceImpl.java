package com.taskmanagement.service.impl;

import com.taskmanagement.dto.request.WorkspaceCreateRequest;
import com.taskmanagement.dto.request.WorkspaceUpdateRequest;
import com.taskmanagement.dto.response.WorkspaceResponse;
import com.taskmanagement.dto.response.WorkspaceStatsResponse;
import com.taskmanagement.entity.User;
import com.taskmanagement.entity.Workspace;
import com.taskmanagement.entity.WorkspaceMember;
import com.taskmanagement.entity.enums.TaskStatus;
import com.taskmanagement.entity.enums.WorkspaceRole;
import com.taskmanagement.exception.BusinessException;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.exception.UnauthorizedException;
import com.taskmanagement.mapper.WorkspaceMapper;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.repository.WorkspaceMemberRepository;
import com.taskmanagement.repository.WorkspaceRepository;
import com.taskmanagement.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceMapper workspaceMapper;

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceCreateRequest request, Long userId) {
        User owner = getUserById(userId);

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        // Add owner as workspace member with OWNER role
        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(savedWorkspace)
                .user(owner)
                .role(WorkspaceRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();

        workspaceMemberRepository.save(ownerMember);

        log.info("Workspace created with id: {} by user: {}", savedWorkspace.getId(), userId);
        return workspaceMapper.toResponse(savedWorkspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(Long workspaceId, WorkspaceUpdateRequest request, Long userId) {
        Workspace workspace = getWorkspaceById(workspaceId);
        validateWorkspaceOwnership(workspace, userId);

        if (request.getName() != null) {
            workspace.setName(request.getName());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }

        Workspace updatedWorkspace = workspaceRepository.save(workspace);
        log.info("Workspace updated with id: {} by user: {}", workspaceId, userId);

        return workspaceMapper.toResponse(updatedWorkspace);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(Long workspaceId, Long userId) {
        Workspace workspace = getWorkspaceById(workspaceId);
        validateWorkspaceAccess(workspaceId, userId);

        return workspaceMapper.toResponse(workspace);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkspaceResponse> getUserWorkspaces(Long userId, Pageable pageable) {
        Page<WorkspaceMember> memberships = workspaceMemberRepository.findByUserId(userId, pageable);
        return memberships.map(membership -> workspaceMapper.toResponse(membership.getWorkspace()));
    }

    @Override
    @Transactional
    public void deleteWorkspace(Long workspaceId, Long userId) {
        Workspace workspace = getWorkspaceById(workspaceId);
        validateWorkspaceOwnership(workspace, userId);

        workspaceRepository.delete(workspace);
        log.info("Workspace deleted with id: {} by user: {}", workspaceId, userId);
    }

    @Override
    @Transactional
    public WorkspaceResponse inviteMember(Long workspaceId, String email, String role, Long userId) {
        Workspace workspace = getWorkspaceById(workspaceId);
        validateWorkspaceAdminAccess(workspaceId, userId);

        User userToInvite = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found with email: " + email));

        // Check if user is already a member
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userToInvite.getId())) {
            throw new BusinessException("User is already a member of this workspace");
        }

        WorkspaceRole workspaceRole;
        try {
            workspaceRole = WorkspaceRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid role: " + role);
        }

        WorkspaceMember newMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(userToInvite)
                .role(workspaceRole)
                .joinedAt(LocalDateTime.now())
                .build();

        workspaceMemberRepository.save(newMember);

        // Send invitation email (would be implemented in EmailService)
        // emailService.sendWorkspaceInvitation(email, workspace.getName(), role);

        log.info("User {} invited to workspace {} with role {}", email, workspaceId, role);
        return workspaceMapper.toResponse(workspace);
    }

    @Override
    @Transactional
    public void removeMember(Long workspaceId, Long memberId, Long userId) {
        validateWorkspaceAdminAccess(workspaceId, userId);

        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace member not found"));

        if (!member.getWorkspace().getId().equals(workspaceId)) {
            throw new BusinessException("Member does not belong to this workspace");
        }

        // Prevent removing the owner
        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new BusinessException("Cannot remove workspace owner");
        }

        // Prevent removing yourself if you're the owner
        if (member.getUser().getId().equals(userId)) {
            throw new BusinessException("Cannot remove yourself from workspace");
        }

        workspaceMemberRepository.delete(member);
        log.info("Member {} removed from workspace {} by user {}", memberId, workspaceId, userId);
    }

    @Override
    @Transactional
    public WorkspaceResponse updateMemberRole(Long workspaceId, Long memberId, String role, Long userId) {
        validateWorkspaceAdminAccess(workspaceId, userId);

        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace member not found"));

        if (!member.getWorkspace().getId().equals(workspaceId)) {
            throw new BusinessException("Member does not belong to this workspace");
        }

        // Prevent changing owner's role
        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new BusinessException("Cannot change owner's role");
        }

        WorkspaceRole newRole;
        try {
            newRole = WorkspaceRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid role: " + role);
        }

        member.setRole(newRole);
        workspaceMemberRepository.save(member);

        log.info("Member {} role updated to {} in workspace {} by user {}", memberId, role, workspaceId, userId);
        return workspaceMapper.toResponse(member.getWorkspace());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceStatsResponse getWorkspaceStats(Long workspaceId, Long userId) {
        validateWorkspaceAccess(workspaceId, userId);

        long totalTasks = taskRepository.countByWorkspaceId(workspaceId);
        long todoTasks = taskRepository.countByWorkspaceIdAndStatus(workspaceId, TaskStatus.TODO);
        long inProgressTasks = taskRepository.countByWorkspaceIdAndStatus(workspaceId, TaskStatus.IN_PROGRESS);
        long doneTasks = taskRepository.countByWorkspaceIdAndStatus(workspaceId, TaskStatus.DONE);

        long totalMembers = workspaceMemberRepository.countByWorkspaceId(workspaceId);

        // Get tasks due in the next 7 days
        LocalDateTime weekFromNow = LocalDateTime.now().plusDays(7);
        List<Object[]> upcomingTasks = taskRepository.findUpcomingTasksWithCount(workspaceId, LocalDateTime.now(), weekFromNow);

        return WorkspaceStatsResponse.builder()
                .totalTasks(totalTasks)
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .doneTasks(doneTasks)
                .totalMembers(totalMembers)
                .completionRate(totalTasks > 0 ? (double) doneTasks / totalTasks * 100 : 0)
                .upcomingTasksCount(upcomingTasks.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getRecentWorkspaces(Long userId, int limit) {
        List<WorkspaceMember> recentMemberships = workspaceMemberRepository
                .findTopByUserIdOrderByJoinedAtDesc(userId, limit);

        return recentMemberships.stream()
                .map(membership -> workspaceMapper.toResponse(membership.getWorkspace()))
                .collect(Collectors.toList());
    }

    private Workspace getWorkspaceById(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private void validateWorkspaceAccess(Long workspaceId, Long userId) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new UnauthorizedException("Access denied to workspace");
        }
    }

    private void validateWorkspaceAdminAccess(Long workspaceId, Long userId) {
        WorkspaceMember membership = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new UnauthorizedException("Access denied to workspace"));

        if (membership.getRole() == WorkspaceRole.VIEWER || membership.getRole() == WorkspaceRole.MEMBER) {
            throw new UnauthorizedException("Insufficient permissions for this operation");
        }
    }

    private void validateWorkspaceOwnership(Workspace workspace, Long userId) {
        if (!workspace.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("Only workspace owner can perform this operation");
        }
    }
}
