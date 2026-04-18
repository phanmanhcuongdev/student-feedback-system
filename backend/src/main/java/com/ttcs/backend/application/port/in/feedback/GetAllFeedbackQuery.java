package com.ttcs.backend.application.port.in.feedback;

import java.time.LocalDate;

public record GetAllFeedbackQuery(
        String keyword,
        String status,
        LocalDate createdDate,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
