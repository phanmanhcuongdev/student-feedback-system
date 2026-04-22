package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StaffFeedbackResponse(
        Integer id,
        Integer studentId,
        String studentName,
        String studentEmail,
        String title,
        String displayContent,
        String originalContent,
        boolean isAutoTranslated,
        String sourceLang,
        LocalDateTime createdAt,
        List<FeedbackResponseView> responses
) {
}
