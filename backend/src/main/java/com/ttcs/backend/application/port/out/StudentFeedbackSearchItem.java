package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;

public record StudentFeedbackSearchItem(
        Integer id,
        String title,
        String content,
        String contentOriginal,
        String contentTranslated,
        String sourceLang,
        String targetLang,
        boolean isAutoTranslated,
        LocalDateTime createdAt
) {
    public StudentFeedbackSearchItem(
            Integer id,
            String title,
            String content,
            String contentOriginal,
            String contentTranslated,
            String sourceLang,
            boolean isAutoTranslated,
            LocalDateTime createdAt
    ) {
        this(id, title, content, contentOriginal, contentTranslated, sourceLang, null, isAutoTranslated, createdAt);
    }
}
