package com.taskmanagement.service;

import com.taskmanagement.dto.response.NotificationResponse;
import com.taskmanagement.dto.response.TaskUpdateMessage;
import com.taskmanagement.entity.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    public void sendTaskAssignmentNotification(Long taskId, Long assigneeId, String taskTitle) {
        String message = String.format("You have been assigned to task: %s", taskTitle);

        // Сохраняем уведомление в БД
        notificationService.createNotification(
                assigneeId,
                "New Task Assignment",
                message,
                NotificationType.TASK_ASSIGNED,
                taskId
        );

        // Отправляем через WebSocket
        String destination = String.format("/user/%d/queue/notifications", assigneeId);
        NotificationResponse notification = NotificationResponse.builder()
                .title("New Task Assignment")
                .message(message)
                .type(NotificationType.TASK_ASSIGNED)
                .entityId(taskId)
                .build();

        messagingTemplate.convertAndSend(destination, notification);
        log.info("WebSocket notification sent to user: {}", assigneeId);
    }

    public void sendTaskUpdateNotification(Long taskId, Long userId, String taskTitle, String updateType) {
        String message = String.format("Task '%s' has been %s", taskTitle, updateType);

        notificationService.createNotification(
                userId,
                "Task Updated",
                message,
                NotificationType.TASK_UPDATED,
                taskId
        );

        String destination = String.format("/user/%d/queue/notifications", userId);
        NotificationResponse notification = NotificationResponse.builder()
                .title("Task Updated")
                .message(message)
                .type(NotificationType.TASK_UPDATED)
                .entityId(taskId)
                .build();

        messagingTemplate.convertAndSend(destination, notification);
    }

    public void sendCommentNotification(Long taskId, Long authorId, String taskTitle, String commentAuthor) {
        String message = String.format("%s commented on task: %s", commentAuthor, taskTitle);

        Long currentUserId = getCurrentUserId();

        // Не отправляем уведомление автору комментария
        if (!authorId.equals(currentUserId)) {
            notificationService.createNotification(
                    authorId,
                    "New Comment",
                    message,
                    NotificationType.TASK_COMMENT,
                    taskId
            );

            String destination = String.format("/user/%d/queue/notifications", authorId);
            NotificationResponse notification = NotificationResponse.builder()
                    .title("New Comment")
                    .message(message)
                    .type(NotificationType.TASK_COMMENT)
                    .entityId(taskId)
                    .build();

            messagingTemplate.convertAndSend(destination, notification);
        }
    }

    public void sendWorkspaceInvitationNotification(Long workspaceId, String workspaceName, String inviteeEmail) {
        // Здесь будет логика отправки приглашения
        // В реальной реализации нужно найти пользователя по email
        log.info("Workspace invitation sent to {} for workspace {}", inviteeEmail, workspaceName);
    }

    public void broadcastTaskUpdate(Long workspaceId, Long taskId, String updateType) {
        String destination = String.format("/topic/workspace-%d/tasks", workspaceId);

        TaskUpdateMessage message = TaskUpdateMessage.builder()
                .taskId(taskId)
                .updateType(updateType)
                .timestamp(System.currentTimeMillis())
                .build();

        messagingTemplate.convertAndSend(destination, message);
        log.info("Task update broadcasted to workspace: {}", workspaceId);
    }

    private Long getCurrentUserId() {
        // Получаем ID текущего пользователя из SecurityContext
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            } else if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    log.warn("Cannot parse user ID from principal: {}", principal);
                }
            }
        }
        return null; // или выбросить исключение, если требуется
    }
}
