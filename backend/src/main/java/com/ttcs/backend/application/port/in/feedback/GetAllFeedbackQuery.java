package com.ttcs.backend.application.port.in.feedback;

import com.ttcs.backend.application.domain.model.Role;

import java.time.LocalDate;

public record GetAllFeedbackQuery(
        String keyword,
        String status,
        LocalDate createdDate,
        Integer viewerUserId,
        Role viewerRole,
        int page,
        int size,
        String sortBy,
        String sortDir
) {
}
