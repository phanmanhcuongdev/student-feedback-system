package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;

public record RealtimeNotificationMessage(
        Integer id,
        String type,
        String title,
        String message,
        Integer surveyId,
        String actionLabel,
        LocalDateTime eventAt
) {
}
