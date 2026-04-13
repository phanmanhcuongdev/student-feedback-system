package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StudentFeedbackResponse(
        Integer id,
        String title,
        String content,
        LocalDateTime createdAt,
        List<FeedbackResponseView> responses
) {
}
