package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;

public record FeedbackResponseView(
        Integer id,
        String responderEmail,
        String responderRole,
        String content,
        LocalDateTime createdAt
) {
}
