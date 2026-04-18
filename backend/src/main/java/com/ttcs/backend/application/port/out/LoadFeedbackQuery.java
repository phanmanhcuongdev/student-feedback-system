package com.ttcs.backend.application.port.out;

import java.time.LocalDate;

public record LoadFeedbackQuery(
        String keyword,
        String status,
        LocalDate createdDate,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
