package com.taskmanagement.security;

import com.taskmanagement.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("workspaceSecurity")
@RequiredArgsConstructor
public class WorkspaceSecurity {

    private final WorkspaceMemberRepository workspaceMemberRepository;

    public boolean isWorkspaceMember(Long workspaceId, Long userId) {
        return workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
    }

    public boolean isWorkspaceAdmin(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(member -> member.getRole().ordinal() <= 1) // OWNER or ADMIN
                .orElse(false);
    }

    public boolean isWorkspaceOwner(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(member -> member.getRole().ordinal() == 0) // OWNER
                .orElse(false);
    }
}
