package com.taskmanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SimpUserRegistry userRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public Set<String> getSubscribedUsersForWorkspace(Long workspaceId) {
        String workspaceDestination = String.format("/topic/workspace-%d", workspaceId);

        return userRegistry.getUsers().stream()
                .flatMap(user -> user.getSessions().stream())
                .flatMap(session -> session.getSubscriptions().stream())
                .filter(sub -> sub.getDestination().equals(workspaceDestination))
                .map(SimpSubscription::getSession)
                .map(session -> session.getUser().getName())
                .collect(Collectors.toSet());
    }

    public void notifyWorkspaceSubscribers(Long workspaceId, Object message) {
        String destination = String.format("/topic/workspace-%d", workspaceId);
        messagingTemplate.convertAndSend(destination, message);
        log.debug("Notification sent to workspace {} subscribers", workspaceId);
    }

    public void notifyTaskSubscribers(Long taskId, Object message) {
        String destination = String.format("/topic/task-%d", taskId);
        messagingTemplate.convertAndSend(destination, message);
        log.debug("Notification sent to task {} subscribers", taskId);
    }

    public boolean isUserOnline(Long userId) {
        return userRegistry.getUsers().stream()
                .anyMatch(user -> user.getName().equals(userId.toString()));
    }

    public Set<Long> getOnlineUsersInWorkspace(Long workspaceId) {
        return getSubscribedUsersForWorkspace(workspaceId).stream()
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }
}
