package com.ttcs.backend.application.port.out;

public record LoadStudentFeedbackQuery(
        Integer studentId,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
