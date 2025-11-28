package com.taskmanagement.util;

import com.taskmanagement.dto.request.TaskCreateRequest;
import com.taskmanagement.dto.request.WorkspaceCreateRequest;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.entity.Workspace;
import com.taskmanagement.entity.WorkspaceMember;
import com.taskmanagement.entity.enums.Priority;
import com.taskmanagement.entity.enums.TaskStatus;
import com.taskmanagement.entity.enums.WorkspaceRole;

import java.time.LocalDateTime;
import java.util.List;

public class TestDataFactory {

    public static User createUser() {
        return User.builder()
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    public static User createUser(Long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .password("password")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    public static Workspace createWorkspace(User owner) {
        return Workspace.builder()
                .name("Test Workspace")
                .description("Test Description")
                .owner(owner)
                .build();
    }

    public static WorkspaceMember createWorkspaceMember(Workspace workspace, User user, WorkspaceRole role) {
        return WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    public static Task createTask(Workspace workspace, User author) {
        return Task.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .workspace(workspace)
                .author(author)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static TaskCreateRequest createTaskCreateRequest(Long workspaceId) {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Test Task");
        request.setDescription("Test Description");
        request.setWorkspaceId(workspaceId);
        request.setPriority(Priority.MEDIUM);
        request.setTags(List.of("test", "urgent"));
        return request;
    }

    public static WorkspaceCreateRequest createWorkspaceCreateRequest() {
        WorkspaceCreateRequest request = new WorkspaceCreateRequest();
        request.setName("Test Workspace");
        request.setDescription("Test Description");
        return request;
    }

    public static Task createTaskWithAssignee(Workspace workspace, User author, User assignee) {
        return Task.builder()
                .title("Assigned Task")
                .description("Task with assignee")
                .status(TaskStatus.IN_PROGRESS)
                .priority(Priority.HIGH)
                .workspace(workspace)
                .author(author)
                .assignee(assignee)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
