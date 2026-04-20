package com.ttcs.backend.application.port.in.admin;

import java.time.LocalDateTime;

public record QuestionBankEntryResult(
        Integer id,
        String content,
        String type,
        String category,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
