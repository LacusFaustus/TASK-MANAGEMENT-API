package com.taskmanagement.service;

import com.taskmanagement.dto.response.NotificationResponse;
import com.taskmanagement.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void createNotification(Long userId, String title, String message, NotificationType type, Long entityId);
    Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
    Long getUnreadCount(Long userId);
    void deleteNotification(Long notificationId, Long userId);
}
