package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;

public record StudentNotificationResponse(
        String type,
        String title,
        String message,
        Integer surveyId,
        String surveyTitle,
        String actionLabel,
        LocalDateTime eventAt
) {
}
