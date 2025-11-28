package com.taskmanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final Map<Long, UserActivity> userActivities = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final SubscriptionService subscriptionService;
    private final WebSocketNotificationService notificationService;

    public void recordUserActivity(Long userId, ActivityType type, Long entityId) {
        UserActivity activity = userActivities.computeIfAbsent(userId,
                id -> UserActivity.builder()
                        .userId(id)
                        .build());

        activity.setLastActivity(System.currentTimeMillis());
        activity.setActivityType(type);
        activity.setEntityId(entityId);

        log.debug("User activity recorded: user={}, type={}, entity={}", userId, type, entityId);

        // Уведомляем о активности пользователя в workspace
        if (entityId != null && type == ActivityType.TASK_VIEW) {
            notifyUserPresence(userId, entityId);
        }
    }

    public UserActivity getUserActivity(Long userId) {
        return userActivities.get(userId);
    }

    public boolean isUserActive(Long userId) {
        UserActivity activity = userActivities.get(userId);
        if (activity == null) {
            return false;
        }

        long inactiveThreshold = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5);
        return activity.getLastActivity() > inactiveThreshold;
    }

    public Map<Long, UserActivity> getActiveUsers() {
        long inactiveThreshold = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5);

        return userActivities.entrySet().stream()
                .filter(entry -> entry.getValue().getLastActivity() > inactiveThreshold)
                .collect(ConcurrentHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        Map::putAll);
    }

    private void cleanupInactiveUsers() {
        long inactiveThreshold = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10);

        userActivities.entrySet().removeIf(entry ->
                entry.getValue().getLastActivity() < inactiveThreshold);

        log.debug("Cleaned up inactive users, remaining: {}", userActivities.size());
    }

    private void notifyUserPresence(Long userId, Long taskId) {
        // Здесь можно отправлять уведомления о присутствии пользователя
        // Например, кто сейчас просматривает задачу
    }

    public enum ActivityType {
        TASK_VIEW,
        TASK_EDIT,
        COMMENT_ADD,
        WORKSPACE_VIEW,
        ONLINE
    }

    @lombok.Data
    @lombok.Builder
    public static class UserActivity {
        private Long userId;
        private Long lastActivity;
        private ActivityType activityType;
        private Long entityId; // taskId, workspaceId, etc.
        private String userAgent;
        private String ipAddress;
    }
}
