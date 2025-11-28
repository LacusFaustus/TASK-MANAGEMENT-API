package com.taskmanagement.repository.specification;

import com.taskmanagement.dto.request.TaskFilterRequest;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.WorkspaceMember;
import com.taskmanagement.entity.enums.TaskStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    public static Specification<Task> withFilter(TaskFilterRequest filter, Long userId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join with workspace and members to check access
            Join<Task, WorkspaceMember> workspaceMemberJoin = root.join("workspace").join("members");
            predicates.add(criteriaBuilder.equal(workspaceMemberJoin.get("user").get("id"), userId));

            if (filter.getWorkspaceId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("workspace").get("id"), filter.getWorkspaceId()));
            }

            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                List<TaskStatus> statuses = filter.getStatus().stream()
                        .map(String::toUpperCase)
                        .map(TaskStatus::valueOf)
                        .toList();
                predicates.add(root.get("status").in(statuses));
            }

            if (filter.getPriority() != null && !filter.getPriority().isEmpty()) {
                predicates.add(root.get("priority").in(filter.getPriority()));
            }

            if (filter.getAssigneeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignee").get("id"), filter.getAssigneeId()));
            }

            if (filter.getAuthorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("author").get("id"), filter.getAuthorId()));
            }

            if (filter.getTags() != null && !filter.getTags().isEmpty()) {
                predicates.add(root.join("tags").in(filter.getTags()));
            }

            if (filter.getDueDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
            }

            if (filter.getDueDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
            }

            if (filter.getCreatedAtFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtFrom()));
            }

            if (filter.getCreatedAtTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtTo()));
            }

            // Full-text search
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchTerm);
                Predicate descriptionPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchTerm);
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            }

            // Overdue tasks
            if (Boolean.TRUE.equals(filter.getOverdue())) {
                predicates.add(criteriaBuilder.lessThan(root.get("dueDate"), LocalDateTime.now()));
                predicates.add(root.get("status").in(TaskStatus.TODO, TaskStatus.IN_PROGRESS));
            }

            query.distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
