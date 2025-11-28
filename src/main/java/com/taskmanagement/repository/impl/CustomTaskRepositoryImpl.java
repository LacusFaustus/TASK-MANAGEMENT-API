package com.taskmanagement.repository.impl;

import com.taskmanagement.entity.Task;
import com.taskmanagement.repository.CustomTaskRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomTaskRepositoryImpl implements CustomTaskRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Page<Task> fullTextSearch(Long workspaceId, String query, Pageable pageable) {
        String sql = """
            SELECT t.*, ts_rank(to_tsvector('english', 
                coalesce(t.title, '') || ' ' || coalesce(t.description, '')), 
                plainto_tsquery('english', :query)) as rank
            FROM tasks t
            WHERE t.workspace_id = :workspaceId
            AND (to_tsvector('english', coalesce(t.title, '') || ' ' || coalesce(t.description, '')) 
                 @@ plainto_tsquery('english', :query))
            ORDER BY rank DESC
            """;

        String countSql = """
            SELECT COUNT(*)
            FROM tasks t
            WHERE t.workspace_id = :workspaceId
            AND (to_tsvector('english', coalesce(t.title, '') || ' ' || coalesce(t.description, '')) 
                 @@ plainto_tsquery('english', :query))
            """;

        @SuppressWarnings("unchecked")
        List<Task> tasks = entityManager.createNativeQuery(sql, Task.class)
                .setParameter("workspaceId", workspaceId)
                .setParameter("query", query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = ((Number) entityManager.createNativeQuery(countSql)
                .setParameter("workspaceId", workspaceId)
                .setParameter("query", query)
                .getSingleResult()).longValue();

        return new PageImpl<>(tasks, pageable, total);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Task> findSimilarTasks(Long workspaceId, List<String> tags, Long excludeTaskId) {
        String sql = """
            SELECT t.*
            FROM tasks t
            JOIN task_tags tt ON t.id = tt.task_id
            WHERE t.workspace_id = :workspaceId
            AND t.id != :excludeTaskId
            AND tt.tag IN :tags
            GROUP BY t.id
            ORDER BY COUNT(tt.tag) DESC, t.created_at DESC
            LIMIT 10
            """;

        return entityManager.createNativeQuery(sql, Task.class)
                .setParameter("workspaceId", workspaceId)
                .setParameter("excludeTaskId", excludeTaskId)
                .setParameter("tags", tags)
                .getResultList();
    }

    @Override
    public Page<Task> findByWorkspaceIdAndTagsIn(Long workspaceId, List<String> tags, Pageable pageable) {
        String sql = """
            SELECT DISTINCT t.*
            FROM tasks t
            JOIN task_tags tt ON t.id = tt.task_id
            WHERE t.workspace_id = :workspaceId
            AND tt.tag IN :tags
            """;

        String countSql = """
            SELECT COUNT(DISTINCT t.id)
            FROM tasks t
            JOIN task_tags tt ON t.id = tt.task_id
            WHERE t.workspace_id = :workspaceId
            AND tt.tag IN :tags
            """;

        @SuppressWarnings("unchecked")
        List<Task> tasks = entityManager.createNativeQuery(sql, Task.class)
                .setParameter("workspaceId", workspaceId)
                .setParameter("tags", tags)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = ((Number) entityManager.createNativeQuery(countSql)
                .setParameter("workspaceId", workspaceId)
                .setParameter("tags", tags)
                .getSingleResult()).longValue();

        return new PageImpl<>(tasks, pageable, total);
    }
}
