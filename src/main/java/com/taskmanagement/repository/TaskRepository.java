package com.taskmanagement.repository;

import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Page<Task> findByWorkspaceId(Long workspaceId, Pageable pageable);

    List<Task> findByAssigneeIdAndStatusIn(Long assigneeId, List<TaskStatus> statuses);

    @Query("""
        SELECT t FROM Task t 
        WHERE t.workspace.id = :workspaceId 
        AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) 
             OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))
             OR EXISTS (SELECT tag FROM t.tags tag WHERE LOWER(tag) LIKE LOWER(CONCAT('%', :query, '%'))))
        """)
    Page<Task> fullTextSearch(@Param("workspaceId") Long workspaceId,
                              @Param("query") String query,
                              Pageable pageable);

    @Query("""
        SELECT t FROM Task t 
        WHERE t.workspace.id = :workspaceId 
        AND t.id != :excludeTaskId
        AND EXISTS (SELECT tag FROM t.tags tag WHERE tag IN :tags)
        """)
    List<Task> findSimilarTasks(@Param("workspaceId") Long workspaceId,
                                @Param("tags") List<String> tags,
                                @Param("excludeTaskId") Long excludeTaskId);

    @Query("""
        SELECT t FROM Task t 
        WHERE t.workspace.id = :workspaceId 
        AND EXISTS (SELECT tag FROM t.tags tag WHERE tag IN :tags)
        """)
    Page<Task> findByWorkspaceIdAndTagsIn(@Param("workspaceId") Long workspaceId,
                                          @Param("tags") List<String> tags,
                                          Pageable pageable);

    Long countByWorkspaceId(Long workspaceId);

    Long countByWorkspaceIdAndStatus(Long workspaceId, TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.workspace.id = :workspaceId AND t.dueDate < CURRENT_TIMESTAMP AND t.status NOT IN ('DONE', 'CANCELLED')")
    Long countOverdueTasksByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("SELECT t FROM Task t WHERE t.workspace.id = :workspaceId " +
            "AND t.dueDate BETWEEN :startDate AND :endDate")
    List<Task> findUpcomingTasksByWorkspace(@Param("workspaceId") Long workspaceId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    boolean existsByIdAndWorkspaceId(Long taskId, Long workspaceId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :userId AND t.dueDate < CURRENT_TIMESTAMP AND t.status NOT IN ('DONE', 'CANCELLED')")
    Long countOverdueTasksByAssigneeId(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.status NOT IN ('DONE', 'CANCELLED')")
    List<Task> findUpcomingTasks(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t.priority, COUNT(t) FROM Task t WHERE t.workspace.id = :workspaceId GROUP BY t.priority")
    List<Object[]> countTasksByPriority(@Param("workspaceId") Long workspaceId);

    @Query(value = """
        SELECT t.due_date as dueDate, COUNT(t.id) as taskCount 
        FROM tasks t 
        WHERE t.workspace_id = :workspaceId 
        AND t.due_date BETWEEN :startDate AND :endDate
        GROUP BY t.due_date
        """, nativeQuery = true)
    List<Object[]> findUpcomingTasksWithCount(@Param("workspaceId") Long workspaceId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :userId AND t.createdAt BETWEEN :startDate AND :endDate")
    Long countByAssigneeIdAndCreatedAtBetween(@Param("userId") Long userId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :userId AND t.status = :status AND t.updatedAt BETWEEN :startDate AND :endDate")
    Long countByAssigneeIdAndStatusAndUpdatedAtBetween(@Param("userId") Long userId,
                                                       @Param("status") TaskStatus status,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.author.id = :userId AND t.createdAt BETWEEN :startDate AND :endDate")
    Long countByAuthorIdAndCreatedAtBetween(@Param("userId") Long userId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // Исправленные запросы для расчета среднего времени выполнения
    @Query(value = """
        SELECT AVG(EXTRACT(EPOCH FROM (updated_at - created_at)) / 3600.0) 
        FROM tasks 
        WHERE workspace_id = :workspaceId 
        AND status = 'DONE'
        """, nativeQuery = true)
    Double getAverageCompletionTime(@Param("workspaceId") Long workspaceId);

    @Query(value = """
        SELECT AVG(EXTRACT(EPOCH FROM (updated_at - created_at)) / 3600.0) 
        FROM tasks 
        WHERE assignee_id = :userId 
        AND status = 'DONE' 
        AND updated_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    Double getAverageCompletionTimeByAssignee(@Param("userId") Long userId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :userId")
    Long countByAssigneeId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :userId AND t.status = :status")
    Long countByAssigneeIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

    @Query(value = """
        SELECT 
            to_char(date_trunc('day', created_at), 'YYYY-MM-DD') as date,
            COUNT(id) as tasks_created,
            COUNT(CASE WHEN status = 'DONE' THEN id END) as tasks_completed
        FROM tasks
        WHERE workspace_id = :workspaceId 
        AND created_at BETWEEN :startDate AND :endDate
        GROUP BY date_trunc('day', created_at)
        ORDER BY date
        """, nativeQuery = true)
    List<Object[]> getTaskTrends(@Param("workspaceId") Long workspaceId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    @Query(value = """
        SELECT 
            to_char(date_trunc('day', updated_at), 'YYYY-MM-DD') as date,
            COUNT(id) as completed_tasks
        FROM tasks
        WHERE assignee_id = :userId 
        AND status = 'DONE'
        AND updated_at BETWEEN :startDate AND :endDate
        GROUP BY date_trunc('day', updated_at)
        ORDER BY date
        """, nativeQuery = true)
    List<Object[]> getDailyCompletedTasks(@Param("userId") Long userId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT 
            t.assignee.id,
            COUNT(t.id),
            COUNT(CASE WHEN t.status = 'DONE' THEN t.id END),
            t.assignee.firstName,
            t.assignee.lastName
        FROM Task t
        WHERE t.workspace.id = :workspaceId
        AND t.assignee IS NOT NULL
        GROUP BY t.assignee.id, t.assignee.firstName, t.assignee.lastName
        """)
    List<Object[]> getTeamPerformanceStats(@Param("workspaceId") Long workspaceId);
}
