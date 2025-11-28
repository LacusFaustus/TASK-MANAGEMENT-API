package com.taskmanagement.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
public class DateTimeUtils {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public String formatDisplayDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DISPLAY_FORMATTER);
    }

    public String formatDisplayDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_FORMATTER);
    }

    public String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (minutes < 1) {
            return "только что";
        } else if (minutes < 60) {
            return minutes + " мин. назад";
        } else if (hours < 24) {
            return hours + " ч. назад";
        } else if (days < 7) {
            return days + " дн. назад";
        } else {
            return formatDisplayDate(dateTime);
        }
    }

    public boolean isOverdue(LocalDateTime dueDate) {
        if (dueDate == null) {
            return false;
        }
        return dueDate.isBefore(LocalDateTime.now());
    }

    public boolean isDueSoon(LocalDateTime dueDate, int hoursThreshold) {
        if (dueDate == null) {
            return false;
        }
        LocalDateTime thresholdTime = LocalDateTime.now().plusHours(hoursThreshold);
        return dueDate.isAfter(LocalDateTime.now()) && dueDate.isBefore(thresholdTime);
    }
}
