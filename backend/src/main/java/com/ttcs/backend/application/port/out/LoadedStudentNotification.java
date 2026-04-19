package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;

public record LoadedStudentNotification(
        Integer id,
        String type,
        String title,
        String message,
        Integer surveyId,
        String surveyTitle,
        String actionLabel,
        LocalDateTime eventAt,
        LocalDateTime readAt
) {
}
