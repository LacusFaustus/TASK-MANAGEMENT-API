package com.taskmanagement.repository;

import com.taskmanagement.entity.WorkspaceMember;
import com.taskmanagement.entity.enums.WorkspaceRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    Page<WorkspaceMember> findByUserId(Long userId, Pageable pageable);

    List<WorkspaceMember> findByWorkspaceId(Long workspaceId);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    long countByWorkspaceId(Long workspaceId);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.user.id = :userId ORDER BY wm.joinedAt DESC LIMIT :limit")
    List<WorkspaceMember> findTopByUserIdOrderByJoinedAtDesc(@Param("userId") Long userId,
                                                             @Param("limit") int limit);

    @Query("SELECT wm.role FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.user.id = :userId")
    Optional<WorkspaceRole> findRoleByWorkspaceIdAndUserId(@Param("workspaceId") Long workspaceId,
                                                           @Param("userId") Long userId);

    void deleteByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.user.id = :userId")
    List<WorkspaceMember> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT wm.user.id) FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId")
    Long countActiveMembers(@Param("workspaceId") Long workspaceId);

    @Query("SELECT COUNT(wm) FROM WorkspaceMember wm WHERE wm.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}
