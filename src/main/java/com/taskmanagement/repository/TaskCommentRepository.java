package com.taskmanagement.repository;

import com.taskmanagement.entity.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    Page<TaskComment> findByTaskIdOrderByCreatedAtDesc(Long taskId, Pageable pageable);

    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    @Query("SELECT c FROM TaskComment c WHERE c.task.workspace.id = :workspaceId ORDER BY c.createdAt DESC")
    Page<TaskComment> findByWorkspaceId(@Param("workspaceId") Long workspaceId, Pageable pageable);

    Long countByTaskId(Long taskId);

    @Query("SELECT c FROM TaskComment c WHERE c.author.id = :userId ORDER BY c.createdAt DESC")
    Page<TaskComment> findByAuthorId(@Param("userId") Long userId, Pageable pageable);

    void deleteByTaskId(Long taskId);

    @Query("SELECT COUNT(c) FROM TaskComment c WHERE c.author.id = :userId AND c.createdAt BETWEEN :startDate AND :endDate")
    Long countByAuthorIdAndCreatedAtBetween(@Param("userId") Long userId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
}
