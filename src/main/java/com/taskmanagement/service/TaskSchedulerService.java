package com.taskmanagement.service;

import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSchedulerService {

    private final TaskService taskService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * ?") // Ежедневно в 9:00
    @Transactional
    public void checkOverdueTasks() {
        log.info("Starting overdue tasks check...");

        // Здесь будет логика проверки просроченных задач
        // В реальной реализации нужно добавить метод в репозиторий

        log.info("Overdue tasks check completed");
    }

    @Scheduled(cron = "0 0 8 * * ?") // Ежедневно в 8:00
    @Transactional
    public void sendDueDateReminders() {
        log.info("Starting due date reminders...");

        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime dayAfterTomorrow = LocalDateTime.now().plusDays(2);

        // Здесь будет логика отправки напоминаний
        // В реальной реализации нужно добавить метод в репозиторий

        log.info("Due date reminders sent");
    }

    @Scheduled(cron = "0 0 * * * ?") // Каждый час
    @Transactional
    public void cleanupOldNotifications() {
        log.info("Cleaning up old notifications...");

        // Очистка прочитанных уведомлений старше 30 дней
        // В реальной реализации нужно добавить метод в репозиторий

        log.info("Old notifications cleanup completed");
    }

    private void sendTaskNotification(Task task, String title, String message, NotificationType type) {
        if (task.getAssignee() != null) {
            notificationService.createNotification(
                    task.getAssignee().getId(),
                    title,
                    message,
                    type,
                    task.getId()
            );
        }

        // Также уведомлять автора задачи
        if (!task.getAuthor().getId().equals(task.getAssignee().getId())) {
            notificationService.createNotification(
                    task.getAuthor().getId(),
                    title,
                    message,
                    type,
                    task.getId()
            );
        }
    }
}
