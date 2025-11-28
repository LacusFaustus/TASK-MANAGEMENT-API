package com.taskmanagement.integration;

import com.taskmanagement.dto.request.TaskCreateRequest;
import com.taskmanagement.dto.request.TaskUpdateRequest;
import com.taskmanagement.dto.response.TaskResponse;
import com.taskmanagement.entity.User;
import com.taskmanagement.entity.Workspace;
import com.taskmanagement.entity.WorkspaceMember;
import com.taskmanagement.entity.enums.WorkspaceRole;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.repository.WorkspaceMemberRepository;
import com.taskmanagement.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class TaskControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    private String authToken;
    private Long workspaceId;
    private Long userId;

    @BeforeEach
    void setUp() {
        // Создание тестового пользователя
        User user = User.builder()
                .email("test@example.com")
                .password("$2a$10$test") // encoded password
                .firstName("Test")
                .lastName("User")
                .build();
        User savedUser = userRepository.save(user);
        this.userId = savedUser.getId();

        // Создание рабочего пространства
        Workspace workspace = Workspace.builder()
                .name("Test Workspace")
                .description("Test Description")
                .owner(savedUser)
                .build();
        Workspace savedWorkspace = workspaceRepository.save(workspace);
        this.workspaceId = savedWorkspace.getId();

        // Добавление пользователя в workspace
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(savedWorkspace)
                .user(savedUser)
                .role(WorkspaceRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();
        workspaceMemberRepository.save(member);

        // Аутентификация и получение токена
        // В реальном тесте здесь был бы вызов API для получения JWT токена
        this.authToken = "mock-jwt-token";
    }

    @Test
    void createTask_WithValidData_ShouldCreateTask() {
        // Given
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Test Task");
        request.setDescription("Test Description");
        request.setWorkspaceId(workspaceId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TaskCreateRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<TaskResponse> response = restTemplate.exchange(
                "/api/v1/tasks",
                HttpMethod.POST,
                entity,
                TaskResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Test Task");
        assertThat(response.getBody().getWorkspaceId()).isEqualTo(workspaceId);
    }

    @Test
    void getTasks_WithValidWorkspace_ShouldReturnTasks() {
        // Given
        createTestTask();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/tasks?workspaceId=" + workspaceId,
                HttpMethod.GET,
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void updateTask_WithValidData_ShouldUpdateTask() {
        // Given
        Long taskId = createTestTask();

        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setTitle("Updated Task Title");
        request.setDescription("Updated Description");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TaskUpdateRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<TaskResponse> response = restTemplate.exchange(
                "/api/v1/tasks/" + taskId,
                HttpMethod.PUT,
                entity,
                TaskResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Task Title");
    }

    private Long createTestTask() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("Test Task for Update");
        request.setDescription("Test Description");
        request.setWorkspaceId(workspaceId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TaskCreateRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<TaskResponse> response = restTemplate.exchange(
                "/api/v1/tasks",
                HttpMethod.POST,
                entity,
                TaskResponse.class
        );

        return response.getBody().getId();
    }
}
