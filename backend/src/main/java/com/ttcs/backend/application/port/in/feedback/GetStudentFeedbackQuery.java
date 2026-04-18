package com.ttcs.backend.application.port.in.feedback;

public record GetStudentFeedbackQuery(
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
