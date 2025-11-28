package com.taskmanagement.controller.workspace;

import com.taskmanagement.dto.response.ApiResponse;
import com.taskmanagement.dto.response.WorkspaceResponse;
import com.taskmanagement.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Workspace Members", description = "API для управления участниками рабочих пространств")
public class WorkspaceMemberController {

    private final WorkspaceService workspaceService;

    @PostMapping("/invite")
    @Operation(summary = "Приглашение участника в рабочее пространство")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> inviteMember(
            @PathVariable Long workspaceId,
            @RequestParam String email,
            @RequestParam String role,
            @AuthenticationPrincipal Long userId) {
        WorkspaceResponse response = workspaceService.inviteMember(workspaceId, email, role, userId);
        return ResponseEntity.ok(ApiResponse.success("Member invited successfully", response));
    }

    @DeleteMapping("/{memberId}")
    @Operation(summary = "Удаление участника из рабочего пространства")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long workspaceId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal Long userId) {
        workspaceService.removeMember(workspaceId, memberId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully"));
    }

    @PutMapping("/{memberId}/role")
    @Operation(summary = "Изменение роли участника")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateMemberRole(
            @PathVariable Long workspaceId,
            @PathVariable Long memberId,
            @RequestParam String role,
            @AuthenticationPrincipal Long userId) {
        WorkspaceResponse response = workspaceService.updateMemberRole(workspaceId, memberId, role, userId);
        return ResponseEntity.ok(ApiResponse.success("Member role updated successfully", response));
    }
}
