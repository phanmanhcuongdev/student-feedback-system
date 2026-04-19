package com.ttcs.backend.adapter.in.web.dto;

import java.time.LocalDateTime;

public record QuestionBankResponse(
        Integer id,
        String content,
        String type,
        String category,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
