package com.ttcs.backend.application.domain.model;

import java.time.LocalDateTime;

public record QuestionBankEntry(
        Integer id,
        String content,
        String type,
        String category,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
