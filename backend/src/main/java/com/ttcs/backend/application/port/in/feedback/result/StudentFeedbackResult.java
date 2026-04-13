package com.ttcs.backend.application.port.in.feedback.result;

import java.time.LocalDateTime;
import java.util.List;

public record StudentFeedbackResult(
        Integer id,
        String title,
        String content,
        LocalDateTime createdAt,
        List<FeedbackResponseResult> responses
) {
}
