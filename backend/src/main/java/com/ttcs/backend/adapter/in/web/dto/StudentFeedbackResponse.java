package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StudentFeedbackResponse(
        Integer id,
        String title,
        String displayContent,
        String originalContent,
        boolean isAutoTranslated,
        String sourceLang,
        LocalDateTime createdAt,
        List<FeedbackResponseView> responses
) {
}
