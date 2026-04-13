package com.ttcs.backend.application.port.in.feedback.result;

import java.time.LocalDateTime;

public record FeedbackResponseResult(
        Integer id,
        Integer feedbackId,
        Integer responderUserId,
        String responderEmail,
        String responderRole,
        String content,
        LocalDateTime createdAt
) {
}
