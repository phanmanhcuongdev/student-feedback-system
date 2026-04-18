package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;

public record StudentFeedbackSearchItem(
        Integer id,
        String title,
        String content,
        LocalDateTime createdAt
) {
}
