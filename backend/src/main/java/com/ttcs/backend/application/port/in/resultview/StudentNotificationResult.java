package com.ttcs.backend.application.port.in.resultview;

import java.time.LocalDateTime;

public record StudentNotificationResult(
        String type,
        String title,
        String message,
        Integer surveyId,
        String surveyTitle,
        String actionLabel,
        LocalDateTime eventAt
) {
}
