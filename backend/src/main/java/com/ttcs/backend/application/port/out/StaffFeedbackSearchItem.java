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
        String contentVi,
        String contentEn,
        String sourceLang,
        boolean isAutoTranslated,
        LocalDateTime createdAt
) {
}
