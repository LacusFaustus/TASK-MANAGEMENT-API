package com.taskmanagement.unit.service;

import com.taskmanagement.dto.request.TaskCreateRequest;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.entity.Workspace;
import com.taskmanagement.entity.WorkspaceMember;
import com.taskmanagement.entity.enums.Priority;
import com.taskmanagement.entity.enums.WorkspaceRole;
import com.taskmanagement.mapper.TaskMapper;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.repository.WorkspaceRepository;
import com.taskmanagement.repository.WorkspaceMemberRepository;
import com.taskmanagement.service.UserActivityService;
import com.taskmanagement.service.WebSocketNotificationService;
import com.taskmanagement.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private WebSocketNotificationService webSocketNotificationService;

    @Mock
    private UserActivityService userActivityService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User testUser;
    private Workspace testWorkspace;
    private Task testTask;
    private TaskCreateRequest taskCreateRequest;
    private WorkspaceMember workspaceMember;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        testWorkspace = Workspace.builder()
                .id(1L)
                .name("Test Workspace")
                .owner(testUser)
                .build();

        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .workspace(testWorkspace)
                .author(testUser)
                .priority(Priority.MEDIUM)
                .build();

        workspaceMember = WorkspaceMember.builder()
                .id(1L)
                .workspace(testWorkspace)
                .user(testUser)
                .role(WorkspaceRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();

        taskCreateRequest = TaskCreateRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .workspaceId(1L)
                .priority(Priority.MEDIUM)
                .build();
    }

    @Test
    void createTask_WithValidData_ShouldCreateTask() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(testWorkspace));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(workspaceMember));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(mock(TaskResponse.class));

        // When
        TaskResponse result = taskService.createTask(taskCreateRequest, 1L);

        // Then
        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(userActivityService, times(1)).recordUserActivity(anyLong(), any(), anyLong());
        verify(webSocketNotificationService, times(1)).broadcastTaskUpdate(anyLong(), anyLong(), anyString());
    }
}
