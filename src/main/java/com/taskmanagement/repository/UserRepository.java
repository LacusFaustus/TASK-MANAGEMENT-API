package com.taskmanagement.repository;

import com.taskmanagement.entity.User;
import com.taskmanagement.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT u FROM User u JOIN u.workspaceMemberships wm " +
            "WHERE wm.workspace.id = :workspaceId AND u.email = :email")
    Optional<User> findByEmailAndWorkspace(@Param("email") String email,
                                           @Param("workspaceId") Long workspaceId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt < :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u FROM User u WHERE u.id IN (" +
            "SELECT t.assignee.id FROM Task t WHERE t.workspace.id = :workspaceId AND t.status IN :statuses)")
    List<User> findActiveAssigneesInWorkspace(@Param("workspaceId") Long workspaceId,
                                              @Param("statuses") List<TaskStatus> statuses);
}
