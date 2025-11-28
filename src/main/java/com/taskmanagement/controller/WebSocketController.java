package com.taskmanagement.controller;

import com.taskmanagement.dto.response.OnlineUserResponse;
import com.taskmanagement.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.Set;

@Controller
@Slf4j
@RequiredArgsConstructor
@Tag(name = "WebSocket", description = "WebSocket API для реального времени")
public class WebSocketController {

    private final SubscriptionService subscriptionService;

    @MessageMapping("/workspaces/{workspaceId}/tasks/{taskId}/typing")
    @SendTo("/topic/workspaces/{workspaceId}/tasks/{taskId}/typing")
    @Operation(summary = "Отправка события набора текста в комментариях")
    public TypingIndicator handleTyping(
            @DestinationVariable Long workspaceId,
            @DestinationVariable Long taskId,
            TypingMessage message,
            @AuthenticationPrincipal Long userId) {

        log.debug("User {} is typing in task {}", userId, taskId);
        return TypingIndicator.builder()
                .userId(userId)
                .taskId(taskId)
                .isTyping(message.isTyping())
                .build();
    }

    @SubscribeMapping("/user/queue/notifications")
    @Operation(summary = "Подписка на уведомления пользователя")
    public void subscribeToUserNotifications(@AuthenticationPrincipal Long userId) {
        log.info("User {} subscribed to notifications", userId);
    }

    @SubscribeMapping("/topic/workspace-{workspaceId}")
    @Operation(summary = "Подписка на события рабочего пространства")
    public void subscribeToWorkspace(
            @DestinationVariable Long workspaceId,
            @AuthenticationPrincipal Long userId) {
        log.info("User {} subscribed to workspace {}", userId, workspaceId);
    }

    @MessageMapping("/workspaces/{workspaceId}/online-users")
    @SendTo("/topic/workspaces/{workspaceId}/online-users")
    @Operation(summary = "Получение списка онлайн пользователей")
    public OnlineUsersResponse getOnlineUsers(@DestinationVariable Long workspaceId) {
        Set<Long> onlineUsers = subscriptionService.getOnlineUsersInWorkspace(workspaceId);

        return OnlineUsersResponse.builder()
                .workspaceId(workspaceId)
                .onlineUsers(onlineUsers)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @MessageMapping("/workspaces/{workspaceId}/tasks/{taskId}/subscribe")
    @Operation(summary = "Подписка на события задачи")
    public void subscribeToTask(
            @DestinationVariable Long workspaceId,
            @DestinationVariable Long taskId,
            @AuthenticationPrincipal Long userId) {
        log.info("User {} subscribed to task {}", userId, taskId);
    }
}

// DTO для WebSocket сообщений
class TypingMessage {
    private boolean typing;
    private Long taskId;

    // getters and setters
    public boolean isTyping() { return typing; }
    public void setTyping(boolean typing) { this.typing = typing; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
}

class TypingIndicator {
    private Long userId;
    private Long taskId;
    private boolean isTyping;

    // builder pattern
    public static TypingIndicatorBuilder builder() { return new TypingIndicatorBuilder(); }

    // getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public boolean isTyping() { return isTyping; }
    public void setTyping(boolean typing) { isTyping = typing; }

    static class TypingIndicatorBuilder {
        private TypingIndicator indicator = new TypingIndicator();

        public TypingIndicatorBuilder userId(Long userId) {
            indicator.userId = userId;
            return this;
        }
        public TypingIndicatorBuilder taskId(Long taskId) {
            indicator.taskId = taskId;
            return this;
        }
        public TypingIndicatorBuilder isTyping(boolean isTyping) {
            indicator.isTyping = isTyping;
            return this;
        }
        public TypingIndicator build() { return indicator; }
    }
}

class OnlineUsersResponse {
    private Long workspaceId;
    private Set<Long> onlineUsers;
    private Long timestamp;

    // builder pattern
    public static OnlineUsersResponseBuilder builder() { return new OnlineUsersResponseBuilder(); }

    // getters and setters
    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }
    public Set<Long> getOnlineUsers() { return onlineUsers; }
    public void setOnlineUsers(Set<Long> onlineUsers) { this.onlineUsers = onlineUsers; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    static class OnlineUsersResponseBuilder {
        private OnlineUsersResponse response = new OnlineUsersResponse();

        public OnlineUsersResponseBuilder workspaceId(Long workspaceId) {
            response.workspaceId = workspaceId;
            return this;
        }
        public OnlineUsersResponseBuilder onlineUsers(Set<Long> onlineUsers) {
            response.onlineUsers = onlineUsers;
            return this;
        }
        public OnlineUsersResponseBuilder timestamp(Long timestamp) {
            response.timestamp = timestamp;
            return this;
        }
        public OnlineUsersResponse build() { return response; }
    }
}
