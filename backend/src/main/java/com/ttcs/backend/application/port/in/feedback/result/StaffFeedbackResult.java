package com.ttcs.backend.application.port.in.feedback.result;

import java.time.LocalDateTime;
import java.util.List;

public record StaffFeedbackResult(
        Integer id,
        Integer studentId,
        String studentName,
        String studentEmail,
        String title,
        String content,
        LocalDateTime createdAt,
        List<FeedbackResponseResult> responses
) {
}
