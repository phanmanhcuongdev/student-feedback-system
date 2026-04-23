package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;

public record StudentFeedbackSearchItem(
        Integer id,
        String title,
        String content,
        String contentOriginal,
        String contentVi,
        String contentEn,
        String sourceLang,
        boolean isAutoTranslated,
        LocalDateTime createdAt
) {
}
