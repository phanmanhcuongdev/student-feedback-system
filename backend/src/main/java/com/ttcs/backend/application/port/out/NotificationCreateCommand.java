package com.ttcs.backend.application.port.out;

import java.util.List;

public record NotificationCreateCommand(
        String type,
        String title,
        String content,
        Integer surveyId,
        String actionLabel,
        Integer createdByUserId,
        String metadata,
        List<Integer> recipientUserIds
) {
}
