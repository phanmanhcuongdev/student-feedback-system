package com.ttcs.backend.application.port.out;

import java.time.LocalDateTime;

public record StaffFeedbackSearchItem(
        Integer id,
        Integer studentId,
        String studentName,
        String studentEmail,
        String title,
        String content,
        String contentOriginal,
        String contentTranslated,
        String sourceLang,
        String targetLang,
        boolean isAutoTranslated,
        LocalDateTime createdAt
) {
    public StaffFeedbackSearchItem(
            Integer id,
            Integer studentId,
            String studentName,
            String studentEmail,
            String title,
            String content,
            String contentOriginal,
            String contentTranslated,
            String sourceLang,
            boolean isAutoTranslated,
            LocalDateTime createdAt
    ) {
        this(id, studentId, studentName, studentEmail, title, content, contentOriginal, contentTranslated, sourceLang, null, isAutoTranslated, createdAt);
    }
}
