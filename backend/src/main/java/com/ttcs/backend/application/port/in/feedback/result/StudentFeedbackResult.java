package com.ttcs.backend.application.port.in.feedback.result;

import java.time.LocalDateTime;
import java.util.List;

public record StudentFeedbackResult(
        Integer id,
        String title,
        String content,
        String contentOriginal,
        String contentVi,
        String contentEn,
        String sourceLang,
        boolean isAutoTranslated,
        LocalDateTime createdAt,
        List<FeedbackResponseResult> responses
) {
}
